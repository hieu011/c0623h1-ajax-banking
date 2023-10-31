package com.cg.service.customer;

import com.cg.model.*;
import com.cg.model.dto.request.TransferReqDTO;
import com.cg.model.dto.response.CustomerResDTO;
import com.cg.repository.*;
import com.cg.service.withdraw.IWithdrawService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class CustomerServiceImpl implements ICustomerService {

    private final ICustomerRepository customerRepository;

    private final IDepositRepository depositRepository;

    private final IWithdrawRepository withdrawRepository;

    private final ITransferRepository transferRepository;

    private final IHistoryRepository historyRepository;

    @Override
    public List<CustomerResDTO> findAllCustomerResDTO() {
        return customerRepository.findAllCustomerResDTO();
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public void create(Customer customer) {
        customer.setBalance(BigDecimal.ZERO);
        customerRepository.save(customer);
    }

    @Override
    public void update(Long id, Customer customer) {
        Customer oldCustomer = findById(customer.getId()).get();
        customer.setBalance(oldCustomer.getBalance());
        customer.setDeleted(oldCustomer.getDeleted());
        customerRepository.save(customer);
    }

    @Override
    public void removeById(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    public void deposit(Deposit deposit) {
        depositRepository.save(deposit);

        Long customerId = deposit.getCustomer().getId();
        BigDecimal transactionAmount = deposit.getTransactionAmount();

        customerRepository.incrementBalance(customerId, transactionAmount);
    }

    @Override
    public void withdraw(Withdraw withdraw) {
        withdrawRepository.save(withdraw);

        Long customerId = withdraw.getCustomer().getId();
        BigDecimal transactionAmount = withdraw.getTransactionAmount();

        customerRepository.decrementBalance(customerId, transactionAmount);
    }

    @Override
    public void transfer(Transfer transfer) {

        Long senderId = transfer.getSender().getId();
        Long recipientId = transfer.getRecipient().getId();

        BigDecimal transferAmount = transfer.getTransferAmount();
        BigDecimal totalBalance = transfer.getTransferAmount().multiply(transfer.getFee()).divide(BigDecimal.valueOf(100)).add(transfer.getTransferAmount());

        customerRepository.decrementBalance(senderId, totalBalance);
        customerRepository.incrementBalance(recipientId, transferAmount);

        transferRepository.save(transfer);

        createHistories(transfer);

    }

    public void createHistories(Transfer transfer) {
        History history = new History();

        history.setSender(transfer.getSender());
        history.setRecipient(transfer.getRecipient());
        history.setTransactionAmount(transfer.getTransferAmount());
        history.setTransactionDate(new Date());

        historyRepository.save(history);
    }

    @Override
    public List<History> findAllHistory() {
        return historyRepository.findAll();
    }


    @Override
    public List<Customer> findAllWithoutId(Long id) {
        List<Customer> customers = customerRepository.findAllByDeleted(false);
        return customers.stream().filter(customer -> !customer.getId().equals(id)).collect(Collectors.toList());
    }

}
