package sanitizr.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "demand")
public class DemandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer day;  // monday(1),tuesday(2)...
    private Integer period;  //8-17(24H)
    private Date time;
    private String toiletId;  //比如M1就可以是M区第一个厕所
}
