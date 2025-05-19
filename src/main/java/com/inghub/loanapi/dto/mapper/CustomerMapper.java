package com.inghub.loanapi.dto.mapper;

import com.inghub.loanapi.dto.CustomerRequest;
import com.inghub.loanapi.dto.CustomerResponse;
import com.inghub.loanapi.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerResponse toResponse(Customer customer);

    Customer toEntity(CustomerRequest request);
}
