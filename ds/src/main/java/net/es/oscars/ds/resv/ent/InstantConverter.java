package net.es.oscars.ds.resv.ent;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Instant;
import java.util.Date;

@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, Date> {

    @Override
    public Date convertToDatabaseColumn(Instant date) {
        Instant instant = Instant.from(date);
        return Date.from(instant);
    }

    @Override
    public Instant convertToEntityAttribute(Date value) {
        Instant instant = value.toInstant();
        return instant;
    }
}
