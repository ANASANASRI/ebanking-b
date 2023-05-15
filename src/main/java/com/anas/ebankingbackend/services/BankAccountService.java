package com.anas.ebankingbackend.services;

import com.anas.ebankingbackend.dtos.CustomerDTO;
import com.anas.ebankingbackend.entities.BankAccount;
import com.anas.ebankingbackend.entities.CurrentAccount;
import com.anas.ebankingbackend.entities.Customer;
import com.anas.ebankingbackend.entities.SavingAccount;
import com.anas.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.anas.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.anas.ebankingbackend.exceptions.CustomerNotFoundException;

import java.util.List;

public interface BankAccountService {
    List<CustomerDTO> listCustomers();
    CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;
    CustomerDTO saveCustomer(CustomerDTO customerDTO);
    BankAccount getBankAccount(String accountId) throws BankAccountNotFoundException;
    List<BankAccount> bankAccountList();

    void deleteCustomer(Long customerId);

    CustomerDTO updateCustomer(CustomerDTO customerDTO);

    CurrentAccount saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException;
    SavingAccount saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException;
    void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
    void credit(String accountId, double amount, String description) throws BankAccountNotFoundException;
    void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException;
}
