package com.anas.ebankingbackend.services;

import com.anas.ebankingbackend.dtos.CustomerDTO;
import com.anas.ebankingbackend.entities.*;
import com.anas.ebankingbackend.enums.OperationType;
import com.anas.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.anas.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.anas.ebankingbackend.exceptions.CustomerNotFoundException;
import com.anas.ebankingbackend.mappers.BankAccountMapperImpl;
import com.anas.ebankingbackend.repositories.AccountOperationRepository;
import com.anas.ebankingbackend.repositories.BankAccountRepository;
import com.anas.ebankingbackend.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {

    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImpl dtoMapper;

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("SAVING NEW CUSTOMER");
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        customerRepository.deleteById(customerId);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("SAVING NEW CUSTOMER");
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CurrentAccount saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        Customer customer =  customerRepository.findById(customerId).orElse(null);
        BankAccount bankAccount;

        if(customer==null){
            throw new CustomerNotFoundException("Customer not found");
        }

        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);

        return bankAccountRepository.save(currentAccount);
    }

    @Override
    public SavingAccount saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        Customer customer =  customerRepository.findById(customerId).orElse(null);
        BankAccount bankAccount;

        if(customer==null){
            throw new CustomerNotFoundException("Customer not found.");
        }

        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);

        return bankAccountRepository.save(savingAccount);
    }

    @Override
    public List<CustomerDTO> listCustomers() {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerDTO> customerDTOS= customers.stream()
                .map(customer -> dtoMapper.fromCustomer(customer)).toList();

        return customerDTOS;
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public List<BankAccount> bankAccountList() {
        return bankAccountRepository.findAll();
    }

    @Override
    public BankAccount getBankAccount(String accountId) throws BankAccountNotFoundException {
        return bankAccountRepository
                .findById(accountId)
                .orElseThrow(()-> new BankAccountNotFoundException("Bank account not found."));
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount = getBankAccount(accountId);

        if(bankAccount.getBalance()<=amount){
            throw new BalanceNotSufficientException("Balance not sufficient");
        }else {
            AccountOperation operation = new AccountOperation();
            operation.setType(OperationType.DEBIT);
            operation.setOperationDate(new Date());
            operation.setAmount(amount);
            operation.setDescription(description);
            operation.setBankAccount(bankAccount);
            accountOperationRepository.save(operation);

            bankAccount.setBalance(bankAccount.getBalance()-amount);
            bankAccountRepository.save(bankAccount);
        }
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount = getBankAccount(accountId);
        AccountOperation operation = new AccountOperation();

        operation.setType(OperationType.CREDIT);
        operation.setOperationDate(new Date());
        operation.setAmount(amount);
        operation.setDescription(description);
        operation.setBankAccount(bankAccount);
        accountOperationRepository.save(operation);

        bankAccount.setBalance(bankAccount.getBalance()+amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource, amount, "Transfer to "+ accountIdDestination);
        credit(accountIdDestination, amount, "Transfer from "+ accountIdSource);
    }

}
