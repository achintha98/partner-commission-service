package com.pm.billingservice.config;

import com.pm.billingservice.dto.StepExecutionReplyDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.integration.partition.RemotePartitioningManagerStepBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

/**
 * @author Achintha Kalunayaka
 * @since 10/9/2025
 */

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@EnableBatchIntegration
@EnableKafka
//@Profile("master")
public class MasterBatchConfig {

    private final JobRepository jobRepository;

    private final PartnerRangePartitioner partitioner;

    private static final Logger logger = LoggerFactory.getLogger(MasterBatchConfig.class);


    @Bean
    public Job monthlyInvoiceJob(Step masterStep) {
        return new JobBuilder("monthlyInvoiceJob", jobRepository)
                .listener(new JobExecutionListener() {
                    private long startTime;

                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        startTime = System.currentTimeMillis();
                        logger.info("Monthly Invoice Job started at {}", LocalDateTime.now());
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        long duration = System.currentTimeMillis() - startTime;
                        logger.info("Monthly Invoice Job finished at {}, total time = {} ms", LocalDateTime.now(), duration);
                    }
                })
                .start(masterStep)
                .build();
    }

    @Bean
    public MessageChannel requests() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel replies() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow outboundKafkaFlow(KafkaTemplate<Object, Object> kafkaTemplate) {
        return IntegrationFlow.from("requests")
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate)
                        .topic("partition-requests"))
                .get();
    }

    @Bean
    public IntegrationFlow inboundKafkaFlow(ConsumerFactory<Object, StepExecutionReplyDto> cf) {
        return IntegrationFlow
                // 1. Consumer expects the safe DTO type
                .from(Kafka.messageDrivenChannelAdapter(cf, "partition-replies"))
                .wireTap(f -> f.handle(m -> {
                    System.out.println("Received Reply DTO: " + m.getPayload());
                }))

                // 2. CRITICAL: Transform DTO back into StepExecution
                .transform(Message.class, m -> {
                    // Cast the received payload to your DTO
                    StepExecutionReplyDto dto = (StepExecutionReplyDto) m.getPayload();

                    // Manually reconstruct the minimal StepExecution object
                    // It requires a JobExecution (using the ID from the DTO)
                    JobExecution minimalJobExecution = new JobExecution(dto.getJobExecutionId());

                    StepExecution stepExecution = new StepExecution(dto.getStepName(), minimalJobExecution);

                    // Set the required properties from the DTO
                    stepExecution.setId(dto.getId());
                    stepExecution.setStatus(dto.getStatus());
                    stepExecution.setExitStatus(new ExitStatus(dto.getExitStatus()));

                    // Return a new Message with the StepExecution as the payload
                    return MessageBuilder
                            .withPayload(stepExecution)
                            .copyHeaders(m.getHeaders())
                            .build();
                })
                // 3. The 'replies' channel now receives the correctly typed object
                .channel("replies")
                .get();
    }

    @Bean
    public Step masterStep(
            MessageChannel requests,
                           MessageChannel replies,
                           BeanFactory beanFactory,
                           JobExplorer jobExplorer) {
        return new RemotePartitioningManagerStepBuilder("master-step", jobRepository)
                .partitioner("generateInvoiceStep", partitioner)
                .gridSize(1) // 8 parallel partitions
                .inputChannel(replies)
                .outputChannel(requests)
                .beanFactory(beanFactory)
                .jobExplorer(jobExplorer)
                .build();
    }

}
