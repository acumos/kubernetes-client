package com.dockerKube.kubernetesclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.dockerKube.*"})
public class KubernetesClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(KubernetesClientApplication.class, args);
	}
}
