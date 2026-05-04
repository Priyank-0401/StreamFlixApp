package com.infy.billing.repository;

import com.infy.billing.entity.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditNoteRepository extends JpaRepository<CreditNote, Long> {
	List<CreditNote> findByInvoice_Id(Long invoiceId);
}