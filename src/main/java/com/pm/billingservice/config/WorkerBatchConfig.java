package com.pm.billingservice.config;

import com.pm.billingservice.dto.StepExecutionReplyDto;
import com.pm.billingservice.dto.StepReplyHolder;
import com.pm.billingservice.model.CommissionPartialAggregate;
import com.pm.billingservice.model.PartnerCommission;
import com.pm.billingservice.repository.CommissionPartialAggregateRepository;
import com.pm.billingservice.repository.PartnerCommissionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Achintha Kalunayaka
 * @since 10/20/2025
 */

@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
@EnableKafka
@RequiredArgsConstructor
//@Profile("worker")
public class WorkerBatchConfig {

    private final JobRepository jobRepository;


    private final PlatformTransactionManager transactionManager;



    private static final Logger logger = LoggerFactory.getLogger(WorkerBatchConfig.class);

    @Bean
    public Step generateInvoiceStep(
                                    MessageChannel newRequests,
                                    MessageChannel newReplies,
                                    JobExplorer jobExplorer,
                                    BeanFactory beanFactory,
                                    ItemReader<PartnerCommission> reader,
                                    ItemProcessor<PartnerCommission, CommissionPartialAggregate> processor,
                                    ItemWriter<CommissionPartialAggregate> writer) {
        return new RemotePartitioningWorkerStepBuilder("generateInvoiceStep", jobRepository)
                .jobExplorer(jobExplorer)
                .inputChannel(newRequests)
                .outputChannel(newReplies)
                .beanFactory(beanFactory)
                .<PartnerCommission, CommissionPartialAggregate>chunk(5, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public MessageChannel newRequests() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel newReplies() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow workerOutboundKafkaFlow(KafkaTemplate<Object, StepExecutionReplyDto> kafkaTemplate) {
        return IntegrationFlow.from("newReplies")
                .wireTap(f -> f.handle(m -> System.out.println("Sending reply: " + m.getPayload())))
                .transform(Message.class, m -> {
                    StepExecution stepExecutionPayload = (StepExecution) m.getPayload();
                    MessageHeaders headers = m.getHeaders();

                    // Build the SAFE DTO using only non-cyclic, necessary data
                    StepExecutionReplyDto stepExecutionReplyDto = StepExecutionReplyDto.builder()
                            // Ensure your DTO only stores primitive IDs, not full objects!
                            .jobExecutionId(stepExecutionPayload.getJobExecution().getId())
                            .status(stepExecutionPayload.getStatus())
                            .exitStatus(stepExecutionPayload.getExitStatus().getExitCode())
                            .stepName(stepExecutionPayload.getStepName())
                            .id(stepExecutionPayload.getId())
                            // Don't forget the Correlation ID if you're using it!
                            // .correlationId(headers.get("correlationId", String.class))
                            .build();

                    // 3. CRITICAL: Return the Message with the DTO as the payload
                    return MessageBuilder
                            .withPayload(stepExecutionReplyDto)
                            .copyHeaders(headers)
                            .build();

                })
                // 4. The outbound adapter will serialize the DTO payload
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate).topic("partition-replies"))
                .get();
        }

    @Bean
    public IntegrationFlow workerInboundKafkaFlow(ConsumerFactory<Object, Object> cf, MessageChannel newRequests) {
        return IntegrationFlow
                .from(Kafka.messageDrivenChannelAdapter(cf, "partition-requests"))
                .wireTap(f -> f.handle(m -> System.out.println(": " + m.getPayload())))
                .channel(newRequests)
                .get();
    }
    @Bean
    @StepScope
    public ItemReader<PartnerCommission> reader(PartnerCommissionRepository partnerCommissionRepository,
                                      @Value("#{stepExecutionContext['minTransactionId']}") Long minId,
                                      @Value("#{stepExecutionContext['maxTransactionId']}") Long maxId) {
        try {
            ItemReader<PartnerCommission> reader =  new RepositoryItemReaderBuilder<PartnerCommission>()
                .name("invoiceItemReader")
                .repository(partnerCommissionRepository)
                .methodName("findByTransactionIdBetween")
                .arguments(List.of(minId, maxId))
                .pageSize(10)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
            return reader;
    } catch (Exception ex) {
        logger.error("Error writing invoices: {}", ex.getMessage(), ex);
        throw ex; // Let Spring Batch mark the step as failed cleanly
    }
    }

    @Bean
    @StepScope
    public ItemProcessor<PartnerCommission, CommissionPartialAggregate> processor(@Value("#{stepExecution.jobExecution.id}") Long jobExecutionId,
                                                                                  @Value("#{stepExecution.id}") Long stepExecutionId) {




        try {
          return partnerCommission -> {

            partnerCommission.setCommissionAmount(partnerCommission.getCommissionAmount().add(BigDecimal.ONE));

            CommissionPartialAggregate commissionPartialAggregate = new CommissionPartialAggregate();
              commissionPartialAggregate.setCommissionAmount(partnerCommission.getCommissionAmount());
              commissionPartialAggregate.setStepExecutionId(stepExecutionId);
            commissionPartialAggregate.setPartnerId(partnerCommission.getPartnerId());
            commissionPartialAggregate.setJobExecutionId(jobExecutionId);
            return commissionPartialAggregate;
        };
    } catch (Exception ex) {
        logger.error("Error processing invoices: {}", ex.getMessage(), ex);
        throw ex; // Let Spring Batch mark the step as failed cleanly
    }
    }

    @Bean
    @StepScope
    public ItemWriter<CommissionPartialAggregate> writer(CommissionPartialAggregateRepository commissionPartialAggregateRepository) {
        return items -> {
            try {
//                Thread.sleep(10000);
                commissionPartialAggregateRepository.saveAll(items);
            } catch (Exception ex) {
                logger.error("Error writing invoices: {}", ex.getMessage(), ex);
                throw ex; // Let Spring Batch mark the step as failed cleanly
            }
        };
    }

    @Bean
    public StepExecutionListener workerStepListener(StepReplyHolder replyHolder) {
        return new StepExecutionListener() {

            private long stepStart;

            @Override
            public void beforeStep(StepExecution stepExecution) {
                stepStart = System.currentTimeMillis();
                logger.info("Worker Step {} started", stepExecution.getStepName());

            }
        };
    }
}
