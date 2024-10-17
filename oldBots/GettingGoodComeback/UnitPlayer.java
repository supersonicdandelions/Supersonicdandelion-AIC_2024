package GettingGoodComeback;

import aic2024.user.CarePackage;
import aic2024.user.StructureType;
import aic2024.user.UnitController;

public class UnitPlayer {

    public void run(UnitController uc) {
        // Code to be executed only at the beginning of the unit's lifespan
            //Case in which we are a HQ
            if (uc.isStructure() && uc.getType() == StructureType.HQ){
                new HQ(uc);
            }

            //Case in which we are an astronaut
            else if (!uc.isStructure()){
                if (uc.getAstronautInfo().getCarePackage() == CarePackage.REINFORCED_SUIT) {
                    new Drone(uc);
                }
                else {
                    new Astronaught(uc);
                }
            }
            else {
                while (true) {
                    uc.yield(); // End of turn
                }
            }
    }
}