package skills.stress

import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession

@SpringBootApplication
class BackendApplication {

	static void main(String[] args) {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			@Override
			boolean verify(String hostname, SSLSession session) {
				return true
			}
		});
		SpringApplication.run(BackendApplication, args)
	}

}
