package com.lightning.modules.lightning;

import com.lightning.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
@NamedEntityGraph(
        name = "Enrollment.withLightningAndGathering",
        attributeNodes = {
                @NamedAttributeNode(value = "lightning", subgraph = "gathering")
        },
        subgraphs = @NamedSubgraph(name = "gathering", attributeNodes = @NamedAttributeNode("gathering"))
)
@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Lightning lightning;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;

}
