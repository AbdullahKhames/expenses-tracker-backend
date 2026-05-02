error in object mapper to work with date time module fixed by adding dependency for jackson
<dependency>
<groupId>com.fasterxml.jackson.datatype</groupId>
<artifactId>jackson-datatype-jsr310</artifactId>
<version>2.17.0</version> <!-- Use the latest version -->
</dependency>

then configuring the ObjectMapper instances
package name.expenses.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

    public class ObjectMapperConfig {
    
        public static ObjectMapper getObjectMapper(){
            ObjectMapper mapper = JsonMapper.builder()
                    .build();
            // Create a custom DateTimeFormatter
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
            // Create a custom serializer with the desired format
            LocalDateTimeSerializer serializer = new LocalDateTimeSerializer(formatter);
    
            // Create a custom deserializer with the desired format
            LocalDateTimeDeserializer deserializer = new LocalDateTimeDeserializer(formatter);
    
            // Register the custom serializer and deserializer
            mapper.registerModule(new JavaTimeModule()
                    .addSerializer(LocalDateTime.class, serializer)
                    .addDeserializer(LocalDateTime.class, deserializer));
            return mapper;
        }
    }


error happened when trying to configure the database from the JNDI
first you must configure the datasource on the application server
then you must configure in the persistence xml to use JTA
        
    <persistence-unit name="expenses-unit" transaction-type="JTA">
    and provde its name 
        <jta-data-source>expenses-db</jta-data-source>

this might trigger an exception for jta transaction manager
solved by adding this property in the properties tag.
    
    <property name="hibernate.transaction.jta.platform" value="org.hibernate.service.jta.platform.internal.SunOneJtaPlatform"/>


