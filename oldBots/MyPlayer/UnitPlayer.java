package MyPlayer;

import aic2024.user.*;

public class UnitPlayer {

    public void run(UnitController uc) {
        // Code to be executed only at the beginning of the unit's lifespan
            //Case in which we are a HQ
            if (uc.isStructure() && uc.getType() == StructureType.HQ){
                new HQ(uc);
            }

            //Case in which we are an astronaut
            else if (!uc.isStructure()){
                new Astronaught(uc);
            }
            else {
                while (true) {
                    uc.yield(); // End of turn
                }
            }
    }
}