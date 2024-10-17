package GotGoodOptimized;

import aic2024.user.*;

import java.util.ArrayList;

public class Drone {
    Direction[] directions = Direction.values();
    Boolean bugNaving = false;
    Boolean followingWall=false;

    ArrayList<Location> structures=new ArrayList<>();

    int closestYet = 10000;

    Direction dir;

    double[][] distanceGraph= new double[13][13];
    public Drone(UnitController uc) {

        int[][][] fillOrder = new int[26][12][];
        int[] indexs = new int[26];

        int dirIndex = (int) (uc.getRandomDouble() * 8.0);
        Direction randomDir = directions[dirIndex];

        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                int v=(i-6)*(i-6)+(j-6)*(j-6);
                distanceGraph[i][j]=Double.POSITIVE_INFINITY;
                if (v<=25) {
                    fillOrder[v][indexs[v]]=new int[]{i,j};
                    indexs[v]++;
                }
            }
        }
        distanceGraph[6][6]=0;

        while (true) {

            BroadcastInfo message = uc.pollBroadcast();
            while (message != null) {
                Location location=new Location(message.getMessage()%(int)Math.pow(2,12)%GameConstants.MAX_MAP_SIZE,(message.getMessage()%(int)Math.pow(2,12))/GameConstants.MAX_MAP_SIZE);

                if (message.getMessage()/(int)Math.pow(2,12)==1) {
                    structures.remove(location);
                }
                else {
                    if (!structures.contains(location)) {
                        structures.add(location);
                    }
                }
                message = uc.pollBroadcast();
            }

            //changed to attack like crazy
            attackThenCollect(uc);

            for (int d=1;d<14;d++) {
                for (int w = 0; w < indexs[d]; w++) {
                    distanceGraph[fillOrder[d][w][0]][fillOrder[d][w][1]] = Double.POSITIVE_INFINITY;
                }
            }

            for (int d=1;d<14;d++) {
                for (int w=0; w<indexs[d];w++) {
                    int i = fillOrder[d][w][0];
                    int j = fillOrder[d][w][1];
                    Location location = uc.getLocation().add(i-6, j-6);
                    if (uc.canSenseLocation(location) && !uc.senseTileType(location).equals(TileType.WATER)) {
                        distanceGraph[i][j]=Math.min(Math.min(Math.min(1+distanceGraph[i+1][j],1+distanceGraph[i-1][j]),Math.min(1+distanceGraph[i][j+1],1+distanceGraph[i][j-1])),Math.min(Math.min(1.4142+distanceGraph[i+1][j+1],1.4142+distanceGraph[i+1][j-1]),Math.min(1.4142+distanceGraph[i-1][j+1],1.4142+distanceGraph[i-1][j-1])));
                    }
                }
            }

            //move randomly, turning right if we can't move.

            if (uc.canPerformAction(ActionType.MOVE,Direction.ZERO,0)) {

                Location closest=graphClosest(uc,uc.senseStructures(GameConstants.ASTRONAUT_VISION_RANGE,uc.getOpponent()));
                //Attack Like Crazy
                if (closest==null) {
                    closest = graphClosest(uc, uc.senseAstronauts(GameConstants.ASTRONAUT_VISION_RANGE, uc.getOpponent()));
                }
                if (uc.getAstronautInfo().getOxygen()<=5 && closest==null) {
                    closest = graphClosest(uc, toPickUp(uc.senseCarePackages(GameConstants.ASTRONAUT_VISION_RANGE)));
                }

                if (closest==null) {
                    if (!structures.isEmpty()) {
                        bugNav(uc, structures);
                    }
                    else {
                        bugNaving = false;
                        if (uc.canPerformAction(ActionType.MOVE, randomDir, 0)) {
                            uc.performAction(ActionType.MOVE, randomDir, 0);
                        }
                        else {
                            ArrayList<Direction> moveable = new ArrayList<>();
                            for (Direction dir : directions) {
                                if (!dir.isEqual(Direction.ZERO)) {
                                    if (uc.canPerformAction(ActionType.MOVE, dir, 0)) {
                                        moveable.add(dir);
                                    }
                                }
                            }
                            if (!moveable.isEmpty()) {
                                randomDir = moveable.get((int) (uc.getRandomDouble() * moveable.size()));
                                uc.performAction(ActionType.MOVE, randomDir, 0);
                            }
                        }
                    }
                }
                else {
                    moveTo(uc,closest);
                }
            }

            broadcastOpponentStructures(uc);

            //changed to attack like crazy
            attackThenCollect(uc);

            //If we have 1 or 2 oxygen left, terraform my tile (alternatively, terraform a random tile)
            if (uc.getAstronautInfo().getOxygen() <= 1) {
                if (uc.canPerformAction(ActionType.TERRAFORM, Direction.ZERO, 0)) {
                    uc.performAction(ActionType.TERRAFORM, Direction.ZERO, 0);
                }
                else {
                    dirIndex = (int) (uc.getRandomDouble() * 8.0);
                    randomDir = directions[dirIndex];
                    for (int i = 0; i < 8; ++i) {
                        //Note that the 'value' of the following command is irrelevant.
                        if (uc.canPerformAction(ActionType.TERRAFORM, randomDir, 0)) {
                            uc.performAction(ActionType.TERRAFORM, randomDir, 0);
                            break;
                        }
                        randomDir = randomDir.rotateRight();
                    }
                }
            }
            uc.yield();
        }
    }
    public double maxOxygen(AstronautInfo[] astronautInfos) {
        double max=0;
        for (AstronautInfo astronaut: astronautInfos) {
            max = Math.max(max,astronaut.getOxygen());
        }
        return max;
    }
    public void attackThenCollect(UnitController uc) {
        StructureInfo[] structureInfos=uc.senseStructures(2,uc.getOpponent());
        while (structureInfos.length>0) {
            for (StructureInfo structure : structureInfos) {
                if (uc.canPerformAction(ActionType.SABOTAGE, uc.getLocation().directionTo(structure.getLocation()), 0)) {
                    uc.performAction(ActionType.SABOTAGE, uc.getLocation().directionTo(structure.getLocation()), 0);
                }
                else {
                    break;
                }
            }
            structureInfos=uc.senseStructures(2,uc.getOpponent());
        }

        double maxOxygen=maxOxygen(uc.senseAstronauts(2,uc.getOpponent()));
        AstronautInfo[] astronautInfo=uc.senseAstronauts(2,uc.getOpponent());
        while (astronautInfo.length>0) {
            for (AstronautInfo astronautOpponent: uc.senseAstronauts(2,uc.getOpponent())) {
                if (uc.canPerformAction(ActionType.SABOTAGE,uc.getLocation().directionTo(astronautOpponent.getLocation()),0) && astronautOpponent.getOxygen()==maxOxygen) {
                    uc.performAction(ActionType.SABOTAGE,uc.getLocation().directionTo(astronautOpponent.getLocation()),0);
                }
                else {
                    break;
                }
            }
            astronautInfo=uc.senseAstronauts(2,uc.getOpponent());
        }

        //Check if there are Care Packages at an adjacent tile. If so, retrieve them.
        if (uc.getAstronautInfo().getOxygen()<=5) {
            for (Direction dir : directions) {
                Location adjLocation = uc.getLocation().add(dir);
                if (!uc.canSenseLocation(adjLocation)) continue;
                CarePackage cp = uc.senseCarePackage(adjLocation);
                if (cp == CarePackage.PLANTS || cp == CarePackage.OXYGEN_TANK || cp == CarePackage.SURVIVAL_KIT || cp == CarePackage.REINFORCED_SUIT) {
                    if (uc.canPerformAction(ActionType.RETRIEVE, dir, 0)) {
                        uc.performAction(ActionType.RETRIEVE, dir, 0);
                        break;
                    }
                }
            }
        }
    }

    public Location graphClosest(UnitController uc,StructureInfo[] structures) {
        Location best=null;
        double distance=Double.POSITIVE_INFINITY;
        for (StructureInfo structure:structures) {
            double value=distanceGraph[structure.getLocation().x-uc.getLocation().x+6][structure.getLocation().y-uc.getLocation().y+6];
            if (value<distance) {
                distance=value;
                best=structure.getLocation();
            }

        }
        return best;
    }
    public Location graphClosest(UnitController uc,AstronautInfo[] astronautInfos) {
        Location best=null;
        double distance=Double.POSITIVE_INFINITY;
        for (AstronautInfo astronautInfo:astronautInfos) {
            double value=distanceGraph[astronautInfo.getLocation().x-uc.getLocation().x+6][astronautInfo.getLocation().y-uc.getLocation().y+6];
            if (value<distance) {
                distance=value;
                best=astronautInfo.getLocation();
            }

        }
        return best;
    }
    public Location graphClosest(UnitController uc,CarePackageInfo[] carePackageInfos) {
        Location best=null;
        double distance=Double.POSITIVE_INFINITY;
        for (CarePackageInfo carePackageInfo:carePackageInfos) {
            double value=distanceGraph[carePackageInfo.getLocation().x-uc.getLocation().x+6][carePackageInfo.getLocation().y-uc.getLocation().y+6];
            if (value<distance) {
                distance=value;
                best=carePackageInfo.getLocation();
            }

        }
        return best;
    }
    public Location graphClosest(UnitController uc,Location[] locations) {
        Location best=null;
        double distance=Double.POSITIVE_INFINITY;
        for (Location location:locations) {
            if (uc.canSenseLocation(location)) {
                double value = distanceGraph[location.x - uc.getLocation().x + 6][location.y - uc.getLocation().y + 6];
                if (value < distance) {
                    distance = value;
                    best = location;
                }
            }
        }
        return best;
    }
    public Direction nextGraphMove(UnitController uc, Location location) {
        if (distanceGraph[location.x-uc.getLocation().x+6][location.y-uc.getLocation().y+6]<(float)1.5) {
            return uc.getLocation().directionTo(location);
        }
        else {
            return nextGraphMove(uc,graphClosest(uc,adjacent(location)));
        }
    }
    public Location[] adjacent(Location location) {
        Location[] toReturn=new Location[9];
        for (int i=0; i<8;i++) {
            toReturn[i]=location.add(directions[i]);
        }
        return toReturn;
    }
    public void moveTo(UnitController uc,Location location) {
        Direction dir=nextGraphMove(uc,location);
        if (dir!=null && uc.canPerformAction(ActionType.MOVE,dir,0)) {
            uc.performAction(ActionType.MOVE, dir, 0);
            bugNaving = false;
            uc.drawPointDebug(location, 100, 0, 0);
        }
    }
    public CarePackageInfo[] toPickUp(CarePackageInfo[] seen) {
        int length =0;
        for (CarePackageInfo carePackageInfo:seen) {
            if (carePackageInfo.getCarePackageType()==CarePackage.OXYGEN_TANK || carePackageInfo.getCarePackageType()==CarePackage.SURVIVAL_KIT || carePackageInfo.getCarePackageType()==CarePackage.PLANTS || carePackageInfo.getCarePackageType()==CarePackage.REINFORCED_SUIT) {
                length++;
            }
        }
        CarePackageInfo[] toReturn = new CarePackageInfo[length];
        int index =0;
        for (CarePackageInfo carePackageInfo:seen) {
            if (carePackageInfo.getCarePackageType()==CarePackage.OXYGEN_TANK || carePackageInfo.getCarePackageType()==CarePackage.SURVIVAL_KIT || carePackageInfo.getCarePackageType()==CarePackage.PLANTS || carePackageInfo.getCarePackageType()==CarePackage.REINFORCED_SUIT) {
                toReturn[index]=carePackageInfo;
                index++;
            }
        }
        return toReturn;
    }
    public void broadcastOpponentStructures(UnitController uc) {
        for (StructureInfo structure: uc.senseStructures(GameConstants.ASTRONAUT_VISION_RANGE,uc.getOpponent())) {
            if (!structures.contains(structure.getLocation())) {
                if (uc.canPerformAction(ActionType.BROADCAST,Direction.ZERO, structure.getLocation().y*GameConstants.MAX_MAP_SIZE+structure.getLocation().x)) {
                    uc.performAction(ActionType.BROADCAST,Direction.ZERO, structure.getLocation().y*GameConstants.MAX_MAP_SIZE+structure.getLocation().x);
                }
            }
        }
        for (Location location: structures) {
            if (uc.canSenseLocation(location) && (uc.senseStructure(location)==null || uc.senseStructure(location).getTeam()!=uc.getOpponent())) {
                if (uc.canPerformAction(ActionType.BROADCAST,Direction.ZERO, (int)Math.pow(2,12)+location.y*GameConstants.MAX_MAP_SIZE+location.x)) {
                    uc.performAction(ActionType.BROADCAST,Direction.ZERO, (int)Math.pow(2,12)+location.y*GameConstants.MAX_MAP_SIZE+location.x);
                }
            }
        }
    }
    public void bugNav(UnitController uc,ArrayList<Location> locations) {
        if (!bugNaving || uc.getLocation().distanceSquared(findNearest(uc,locations))<closestYet) {
            closestYet=uc.getLocation().distanceSquared(findNearest(uc,locations));
            bugNaving=true;
            Direction greedyDir=uc.getLocation().directionTo(findNearest(uc,locations));
            if (uc.canPerformAction(ActionType.MOVE,greedyDir,0)) {
                uc.performAction(ActionType.MOVE,greedyDir,0);
            }
            else {
                followingWall=true;
                dir=greedyDir;
                for (int i = 0; i < 8; ++i){
                    //Note that the 'value' of the following command is irrelevant.
                    if (uc.canPerformAction(ActionType.MOVE, dir, 0)){
                        uc.performAction(ActionType.MOVE, dir, 0);
                        break;
                    }
                    dir = dir.rotateLeft();

                }
            }

        }
        else {
            if (followingWall) {
                dir = dir.opposite();
                for (int i = 0; i < 8; i++) {
                    dir = dir.rotateLeft();
                    if (uc.canPerformAction(ActionType.MOVE, dir, 0)){
                        uc.performAction(ActionType.MOVE, dir, 0);
                        break;
                    }
                }

            }

            else {
                Direction greedyDir=uc.getLocation().directionTo(findNearest(uc,locations));
                if (uc.canPerformAction(ActionType.MOVE,greedyDir,0)) {
                    uc.performAction(ActionType.MOVE,greedyDir,0);
                }
                else {
                    followingWall=true;
                    dir=greedyDir;
                    for (int i = 0; i < 8; ++i){
                        //Note that the 'value' of the following command is irrelevant.
                        if (uc.canPerformAction(ActionType.MOVE, dir, 0)){
                            uc.performAction(ActionType.MOVE, dir, 0);
                            break;
                        }
                        dir = dir.rotateLeft();

                    }
                }
            }
        }
    }
    public Location findNearest(UnitController uc,ArrayList<Location> locations) {
        Location closest=null;
        double best=Double.POSITIVE_INFINITY;
        for (Location location: locations) {
            if (uc.getLocation().distanceSquared(location)<best) {
                closest=location;
                best=uc.getLocation().distanceSquared(location);
            }
        }
        return closest;
    }
}
