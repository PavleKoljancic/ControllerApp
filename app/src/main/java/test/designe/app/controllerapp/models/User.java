package test.designe.app.controllerapp.models;

import java.math.BigDecimal;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class User {

    private Integer id;
    String pictureHash;
    private String firstName;
    private String lastName;


}
