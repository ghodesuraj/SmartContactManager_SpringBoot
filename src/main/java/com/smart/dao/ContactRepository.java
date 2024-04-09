package com.smart.dao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.smart.entities.Contact;

public interface ContactRepository extends JpaRepository< Contact, Integer>{
	
	//Pagination
	@Query("from Contact as c where c.user.id = :userId")
	//currentPage-page
	//contact Per page - 5
	public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);
	
	@Modifying
    @Transactional
    @Query("delete from Contact c where c.cId =:id")
    public void deleteContactById(@Param("id") int id);

}
