package miu.edu.processor;

import miu.edu.dto.DataDTO;
import miu.edu.model.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.time.Instant;
import java.time.LocalDate;

public class DataItemProcessor implements ItemProcessor<DataDTO, Data> {

    private static final Logger log = LoggerFactory.getLogger(DataItemProcessor.class);

    @Override
    public Data process(final DataDTO data) throws Exception {
        final String firstName = data.getFirst().toUpperCase();
        final String lastName = data.getLast().toUpperCase();
        final Double gpa = data.getGpa();
        final Integer age = data.getAge();
        final LocalDate dob = LocalDate.ofYearDay(LocalDate.now().getYear() - age, 1);

        final Data transformedData = new Data(null, firstName, lastName, gpa, dob);

        log.info("Converting (" + data + ") into (" + transformedData + ")");

        return transformedData;
    }

}
