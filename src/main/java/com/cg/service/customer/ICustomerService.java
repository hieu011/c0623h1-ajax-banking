package com.cg.service.customer;

import com.cg.model.*;
import com.cg.model.dto.response.CustomerResDTO;
import com.cg.service.IGeneralService;

import java.util.List;

public interface ICustomerService extends IGeneralService<Customer, Long> {

    List<CustomerResDTO> findAllCustomerResDTO();

    void deposit (Deposit deposit);

    void withdraw (Withdraw withdraw);

    void transfer (Transfer transfer);

    List<History> findAllHistory();

    List<Customer> findAllWithoutId(Long id);
}
