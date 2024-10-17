package oldBots.transferBot;

import aic2024.user.*;

import java.util.ArrayList;

public class HQ {
    Direction[] directions = Direction.values();

    ArrayList<Location> structures=new ArrayList<>();

    public HQ(UnitController uc) {
        int spawnValue=Math.max(uc.getMapWidth(), uc.getMapHeight())+10;
        while (true) {
            // if enemies are seen, spam like crazy, especially drones
            if (uc.senseAstronauts(GameConstants.HQ_VISION_RANGE,uc.getOpponent()).length>0) {
                for (Direction dir : directions) {
                    if (uc.canEnlistAstronaut(dir, 100, CarePackage.REINFORCED_SUIT)) {

                        uc.enlistAstronaut(dir, 100, CarePackage.REINFORCED_SUIT);
                    }
                    else if (uc.canEnlistAstronaut(dir,(int)GameConstants.MIN_OXYGEN_ASTRONAUT,null)){
                            uc.enlistAstronaut(dir, (int)GameConstants.MIN_OXYGEN_ASTRONAUT, null);
                    }

                }
            }
            //Spawn exactly one astronaut with 30 oxygen, if possible

            if (uc.senseAstronauts(GameConstants.HQ_VISION_RANGE,uc.getTeam()).length<15) {
                for (Direction dir : directions) {
                    if (uc.canEnlistAstronaut(dir, spawnValue, null)) {
                        if (uc.getStructureInfo().getCarePackagesOfType(CarePackage.SURVIVAL_KIT) > 0) {
                            uc.enlistAstronaut(dir, spawnValue / 2, CarePackage.SURVIVAL_KIT);
                        } else if (uc.getStructureInfo().getCarePackagesOfType(CarePackage.REINFORCED_SUIT) > 0) {
                            uc.enlistAstronaut(dir, spawnValue, CarePackage.REINFORCED_SUIT);
                        } else {
                            uc.enlistAstronaut(dir, spawnValue / 2, null);
                        }
                        break;
                    }
                }
            }

            BroadcastInfo message = uc.pollBroadcast();
            while (message != null) {
                uc.println(message.getMessage());
                Location location=new Location(message.getMessage()%(int)Math.pow(2,12)%GameConstants.MAX_MAP_SIZE,(message.getMessage()%(int)Math.pow(2,12))/GameConstants.MAX_MAP_SIZE);

                if (message.getMessage()/(int)Math.pow(2,12)==1) {
                    structures.remove(location);
                }
                else {
                    if (!structures.contains(location)) {
                        structures.add(location);
                        uc.println(location.x);
                        uc.println(location.y);
                    }
                }
                message = uc.pollBroadcast();
            }
            for (Location location:structures) {
                if (uc.canPerformAction(ActionType.BROADCAST,Direction.ZERO,location.y*GameConstants.MAX_MAP_SIZE+location.x)) {
                    uc.performAction(ActionType.BROADCAST,Direction.ZERO,location.y*GameConstants.MAX_MAP_SIZE+location.x);
                }
            }

            broadcastOpponentStructures(uc);
            uc.yield();
        }
    }
    public void broadcastOpponentStructures(UnitController uc) {
        for (StructureInfo structure: uc.senseStructures(GameConstants.HQ_VISION_RANGE,uc.getOpponent())) {
            if (!structures.contains(structure.getLocation())) {
                if (uc.canPerformAction(ActionType.BROADCAST,Direction.ZERO, structure.getLocation().y*GameConstants.MAX_MAP_SIZE+structure.getLocation().x)) {
                    uc.performAction(ActionType.BROADCAST,Direction.ZERO, structure.getLocation().y*GameConstants.MAX_MAP_SIZE+structure.getLocation().x);
                    structures.add(structure.getLocation());
                }
            }
        }
    }
}