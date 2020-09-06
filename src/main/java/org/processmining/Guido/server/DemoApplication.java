package org.processmining.Guido.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class DemoApplication {
	public static void main(String[] args) {
		final Path data = Paths.get("./data");
		if (!Files.exists(data)) {
			try {
				Files.createDirectory(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		SpringApplication.run(DemoApplication.class, args);
	}

}
