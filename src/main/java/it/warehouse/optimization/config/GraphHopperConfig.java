package it.warehouse.optimization.config;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import static com.graphhopper.json.Statement.If;
import static com.graphhopper.json.Statement.Op.LIMIT;
import static com.graphhopper.json.Statement.Op.MULTIPLY;


import java.util.List;

@Getter
@ApplicationScoped
@Startup
public class GraphHopperConfig {

    private GraphHopper hopper;

    @PostConstruct
    public void init() {
        hopper = new GraphHopper();
        hopper.setOSMFile("data/italy-latest.osm.pbf");
        hopper.setGraphHopperLocation("graph-cache");
        hopper.setEncodedValuesString("surface,car_average_speed");

        CustomModel customModel = new CustomModel();

        customModel.addToSpeed(If("true", LIMIT, "car_average_speed"));

        customModel.addToSpeed(If("surface == ASPHALT", MULTIPLY, "1.1"));

        Profile carProfile = new Profile("car")
                .setWeighting("custom")
                .setCustomModel(customModel);

        hopper.setProfiles(carProfile);

        // Importazione o caricamento del grafo
        hopper.importOrLoad();
    }
}
