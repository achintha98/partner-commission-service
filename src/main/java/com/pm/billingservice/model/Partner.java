package com.pm.billingservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * @author Achintha Kalunayaka
 * @since 4/7/2025
 */

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @Email
    @NotNull
    @Column(unique = true)
    private String email;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Invoice> invoices;

    @NotNull
    private String address;

    private String partnerTier;

//    @NotNull
//    @Enumerated(EnumType.STRING)
//    private String patientStatus;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private LocalDate registeredDate;
}
