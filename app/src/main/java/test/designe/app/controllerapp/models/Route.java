package test.designe.app.controllerapp.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class Route {
    

    private Integer id;
    private String name;
    private  Boolean isActive = true;
    private Integer transporterId;
}
