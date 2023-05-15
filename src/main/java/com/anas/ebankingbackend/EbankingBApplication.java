package com.anas.ebankingbackend;

import com.anas.ebankingbackend.dtos.CustomerDTO;
import com.anas.ebankingbackend.entities.*;
import com.anas.ebankingbackend.enums.AccountStatus;
import com.anas.ebankingbackend.enums.OperationType;
import com.anas.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.anas.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.anas.ebankingbackend.exceptions.CustomerNotFoundException;
import com.anas.ebankingbackend.repositories.AccountOperationRepository;
import com.anas.ebankingbackend.repositories.BankAccountRepository;
import com.anas.ebankingbackend.repositories.CustomerRepository;
import com.anas.ebankingbackend.services.BankAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EbankingBApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbankingBApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(BankAccountService bankAccountService){
		return args -> {
			Stream.of("Anas","Imane","Mohamed").forEach(name->{
				CustomerDTO customer=new CustomerDTO();
				customer.setNom(name);
				customer.setEmail(name+"@gmail.com");
				bankAccountService.saveCustomer(customer);

			});

			bankAccountService.listCustomers().forEach(customer -> {
				try{
					bankAccountService.saveCurrentBankAccount(Math.random()*90000, 9000, customer.getId());
					bankAccountService.saveSavingBankAccount(Math.random()*30000,3.2, customer.getId());
					List<BankAccount> bankAccounts = bankAccountService.bankAccountList();

					for (BankAccount account :
							bankAccounts) {
						for (int i = 0; i < 10; i++) {
							bankAccountService.credit(account.getId(), 10000*Math.random()*120, "CREDIT");
							bankAccountService.debit(account.getId(), 1500*Math.random(), "DEBIT");
						}
					}
				}
				catch (CustomerNotFoundException | BalanceNotSufficientException | BankAccountNotFoundException e){
					e.printStackTrace();
				}

			});
		};
	}

	//@Bean
	CommandLineRunner start(BankAccountRepository bankAccountRepository,
							CustomerRepository customerRepository,
							AccountOperationRepository accountOperationRepository){
		return args -> {
			Stream.of("Oussama", "Amine","Rachid").forEach(name->{
				Customer customer = new Customer();
				customer.setNom(name);
				customer.setEmail(name+"gmail.com");
				customerRepository.save(customer);
			});

			customerRepository.findAll().forEach(customer -> {
				CurrentAccount currentAccount =  new CurrentAccount();
				currentAccount.setId(UUID.randomUUID().toString());
				currentAccount.setBalance(Math.random()*320000);
				currentAccount.setCreatedAt(new Date());
				currentAccount.setOverDraft(89000);
				currentAccount.setCustomer(customer);
				currentAccount.setStatus(AccountStatus.CREATED);
				bankAccountRepository.save(currentAccount);

				SavingAccount savingAccount =  new SavingAccount();
				savingAccount.setId(UUID.randomUUID().toString());
				savingAccount.setBalance(Math.random()*320000);
				savingAccount.setCreatedAt(new Date());
				savingAccount.setCustomer(customer);
				savingAccount.setInterestRate(9.3);
				savingAccount.setStatus(AccountStatus.CREATED);
				bankAccountRepository.save(savingAccount);
			});

			bankAccountRepository.findAll().forEach(account->{
				for (int i = 0; i < 5; i++) {
					AccountOperation operation = new AccountOperation();
					operation.setAmount(Math.random()*55000);
					operation.setOperationDate(new Date());
					operation.setType(Math.random() > 0.5 ? OperationType.DEBIT : OperationType.CREDIT);
					operation.setBankAccount(account);
					accountOperationRepository.save(operation);
				}
			});
		};

	}
}

