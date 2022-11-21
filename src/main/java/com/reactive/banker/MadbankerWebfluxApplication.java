package com.reactive.banker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.log4j.Log4j2;

@SpringBootApplication
@Slf4j
public class MadbankerWebfluxApplication {

	public static void main(String[] args) {
		log.debug("in start........");

		SpringApplication.run(MadbankerWebfluxApplication.class, args);


	}

}
