package miu.edu.config;

import lombok.RequiredArgsConstructor;
import miu.edu.dto.DataDTO;
import miu.edu.model.Data;
import miu.edu.processor.DataItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfiguration {

    public final JobBuilderFactory jobBuilderFactory;

    public final StepBuilderFactory stepBuilderFactory;

    private final DataSource dataSource;

    @Bean
    public FlatFileItemReader<DataDTO> reader() {
        return new FlatFileItemReaderBuilder<DataDTO>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()
                .names("first", "last", "gpa", "age")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<DataDTO>() {{
                    setTargetType(DataDTO.class);
                }})
                .build();
    }

    @Bean
    public DataItemProcessor processor() {
        return new DataItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Data> writer() {
        return new JdbcBatchItemWriterBuilder<Data>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO data (first, last, gpa, dob) VALUES (:first, :last, :gpa, :dob)")
                .dataSource(dataSource)
                .build();
    }

    @Bean(name="importData")
    public Job importDataJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importDataJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(JdbcBatchItemWriter<Data> writer) {
        return stepBuilderFactory.get("step1")
                .<DataDTO, Data> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }


}