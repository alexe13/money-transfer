package ga.fundamental.revolut.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

@Data
@Entity
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue
    private Long id;
    private BigDecimal balance;
}
