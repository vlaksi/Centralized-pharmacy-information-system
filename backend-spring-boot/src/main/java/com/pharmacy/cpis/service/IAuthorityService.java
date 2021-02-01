package com.pharmacy.cpis.service;

import com.pharmacy.cpis.model.Authority;

import java.util.List;

public interface IAuthorityService {

    List<Authority> findById(Long id);

    List<Authority> findByName(String name);
}
