package assignment6.ParkingLot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import assignment6.BiMap.BiMap;
import assignment6.ParkingLot.enums.RearrangeState;
import assignment6.ParkingLot.enums.SpaceState;

public class ParkingLot {

    private BiMap<Space,Car> carSpaceBiMap;

    public ParkingLot(HashMap<Space,Car> spaceToCar) {
        carSpaceBiMap = new BiMap<Space,Car>(spaceToCar);
    }

    //* Solution 1 */////////////////////////////////////////////////////////
    // Simple to understand, brute-force solution using swaps.  in O(n)
    // Time efficiency is O(n),  Number of car swaps is O(n)
    public List<CarMove> rearrange(ParkingLot goalState) throws IllegalArgumentException {
        if (!isRearrangementOfParkingLot(goalState)) {
            throw new IllegalArgumentException(
                "The goal parking lot state has to be a rearrangement of the initial parking lot state.");
        }

        List<CarMove> carMoveList = new ArrayList<>();

        // for each space, the loop put the car that should be there, according to the "another" parking lot
        // In iteration i - there are i parking spaces that are in order according to "another" parking lot
        // ,so in iteration n we know for sure that all spaces are in order according to "another" parking lot
        // Because each iteration time efficiency is O(1), than n iterations are O(n)
        for (Space space : carSpaceBiMap.getKeySet()) {
            Car carToMove = goalState.getCarBySpace(space);
            Car carToRemove = this.getCarBySpace(space);

            if (carToMove.equals(Car.noCar)) {
                moveCarToEmptySpace(carToRemove, carMoveList);
            }
            else if (carToRemove.equals(Car.noCar)) {
                moveCarToEmptySpace(carToMove, carMoveList);
            }
            else {
                swapCarsSpacesThroughEmptySpace(carToMove, carToRemove, carMoveList);
            }
        }
        return carMoveList;
    }
    private void swapCarsSpacesThroughEmptySpace(Car car1, Car car2, List<CarMove> carMoveList) {
        Space car1Space = getSpaceByCar(car1);
        Space car2Space = getSpaceByCar(car2);
        Space emptySpace = getEmptySpace();

        carMoveList.add(makeMove(car1,car1Space,emptySpace));
        carMoveList.add(makeMove(car2,car2Space,car1Space));
        carMoveList.add(makeMove(car1,emptySpace,car2Space));
        makeMove(Car.noCar,null, emptySpace);
    }
    // move with updating a List<CarMove> with the new car move
    private void moveCarToEmptySpace(Car car, List<CarMove> carMoveList) {
        Space toBeFilledEmptySpace = getEmptySpace();
        Space toBeEmptyFilledSpace = getSpaceByCar(car);

        CarMove carMove = makeMove(car,toBeEmptyFilledSpace,toBeFilledEmptySpace);
        if(carMoveList != null)
        {
            carMoveList.add(carMove);
        }
        makeMove(Car.noCar,null, toBeEmptyFilledSpace);
    }
    // move without updating a List<CarMove> with the new car move
    private boolean isRearrangementOfParkingLot(ParkingLot another) {
        return   another.carSpaceBiMap.getKeySet().equals(carSpaceBiMap.getKeySet()) &&
                another.carSpaceBiMap.getValueSet().equals(carSpaceBiMap.getValueSet()) ;
    }
    private CarMove makeMove(Car car, Space from, Space to) {
        carSpaceBiMap.put(to, car);
        if (!car.equals(Car.noCar)) {
            return new CarMove(car,from,to);
        }
        return null;
    }


    //* Solution 2 */////////////////////////////////////////////////////////
    // O(n) circuit-dependencies finder solution using DFS.
    // O(n) car-moves, but less actual moves than solution 1 "rearrange"
    public List<CarMove> rearrangeInFewerSteps(ParkingLot another) {
        List<CarMove> carMoves = new ArrayList<>();
        Map<Car, RearrangeState> carStateMap = new HashMap<>();
        Map<Space, SpaceState> spaceStateMap = new HashMap<>();
        initStateMaps(carStateMap, spaceStateMap, another);
        rearrangeInFewerStepsUtil(carMoves, another, carStateMap, spaceStateMap);
        return carMoves;
    }
    private void rearrangeInFewerStepsUtil(List<CarMove> carMoves, ParkingLot another,
                                           Map<Car, RearrangeState> carStateMap,
                                           Map<Space, SpaceState> spaceStateMap) {
        for (Car car : carSpaceBiMap.getValueSet()) {
            if (!car.equals(Car.noCar) && carStateMap.get(car).equals(RearrangeState.NOT_REARRANGED)) {
                rearrangeInFewerStepsUtilRecursive(carMoves, car, another, carStateMap, spaceStateMap);
            }
        }
    }
    private void rearrangeInFewerStepsUtilRecursive(List<CarMove> carMoves, Car car, ParkingLot another, Map<Car,
            RearrangeState> carStateMap, Map<Space, SpaceState> spaceStateMap) {
        RearrangeState currentState = carStateMap.get(car);

        if (currentState.equals(RearrangeState.NOT_REARRANGED)) {
            carStateMap.put(car, RearrangeState.REARRANGING);
            Space desiredSpace = another.getCarSpaceBiMap().getKey(car);
            if (spaceStateMap.get(desiredSpace).equals(SpaceState.NOT_EMPTY)) {
                rearrangeInFewerStepsUtilRecursive(carMoves, carSpaceBiMap.getValue(desiredSpace), another,
                        carStateMap, spaceStateMap);
            }
            if (spaceStateMap.get(desiredSpace).equals(SpaceState.EMPTY)) {
                carMoves.add(new CarMove(car, carSpaceBiMap.getKey(car),
                        another.getCarSpaceBiMap().getKey(car)));
                updateCarSpaceStates(car, desiredSpace, true,  carStateMap, spaceStateMap);
                moveCarToEmptySpace(car);
            }
        }

        if (currentState.equals(RearrangeState.REARRANGING)) {
            Space emptySpace = getEmptySpace();
            carMoves.add(new CarMove(car, carSpaceBiMap.getKey(car), emptySpace));
            updateCarSpaceStates(car, emptySpace, false,  carStateMap, spaceStateMap);
            moveCarToEmptySpace(car);
        }
    }
    private void updateCarSpaceStates(Car car, Space emptySpace, boolean isCarRearranged,
                                      Map<Car, RearrangeState> carStateMap, Map<Space, SpaceState> spaceStateMap) {
        spaceStateMap.put(emptySpace, SpaceState.NOT_EMPTY);
        spaceStateMap.put(carSpaceBiMap.getKey(car), SpaceState.EMPTY);
        if (isCarRearranged) {
            carStateMap.put(car, RearrangeState.REARRANGED);
        } else {
            carStateMap.put(car, RearrangeState.NOT_REARRANGED);
        }
    }
    private void initStateMaps(Map<Car, RearrangeState> carStateMap, Map<Space, SpaceState> spaceStateMap, ParkingLot another) {
        carSpaceBiMap.getKeySet()
                .forEach(e -> {
                    Car before = carSpaceBiMap.getValue(e);
                    Car after = another.getCarSpaceBiMap().getValue(e);
                    carStateMap.put(before, RearrangeState.NOT_REARRANGED);
                    spaceStateMap.put(e, SpaceState.NOT_EMPTY);
                    if(before != Car.noCar && after != Car.noCar) {
                        if (before.equals(after)) {
                            carStateMap.put(before, RearrangeState.REARRANGED);
                        }
                    }
                });
        spaceStateMap.put(getEmptySpace(), SpaceState.EMPTY);
    }
    public void moveCarToEmptySpace(Car car) {
        moveCarToEmptySpace(car,null);
    }
    
    
    
    
    // Solution to challenge 1 and 2 ===============================================================

    /**
     * Finds a list of moves to rearrange cars from an initial state to a goal state.
     * This method assumes there is exactly one empty space.
     * @param initialState a ParkingLot with exactly one empty space
     * @param goalState    a rearrangement of the initial ParkingLot
     * @return a list of moves
     */
    public List<Move> getCarMoves(ParkingLot goalState) {
      if (!isRearrangementOfParkingLot(goalState)) {
        throw new IllegalArgumentException(
            "The goal parking lot state has to be a rearrangement of the initial parking lot state.");
      }

      List<Move> moves = new ArrayList<Move>();
      ParkingLot currentState = new ParkingLot(carSpaceBiMap.getKeyToValueMap());
      moveCars(currentState, goalState, moves);
      return moves;
    }

    private static void moveCars(ParkingLot currentState, ParkingLot goalState, List<Move> moves) {
      for (int i = 0; i < currentState.getSize(); i++) {
        Space emptySpace = currentState.getEmptySpace();

        if (!spaceShouldBeEmpty(emptySpace, goalState)) {
          Car car = getGoalCar(emptySpace, goalState);
          moveCarToEmptySpace(car, currentState, moves);
        } else {
          for (Car car : currentState.getCarSpaceBiMap().getValueSet()) {
            if (carIsInWrongSpace(car, currentState, goalState)) {
              moveCarToEmptySpace(car, currentState, moves);
              moveCars(currentState, goalState, moves);
            }
          }
        }
      }
    }

    private static boolean spaceShouldBeEmpty(Space emptySpace, ParkingLot goalState) {
      return goalState.getCarBySpace(emptySpace) == Car.noCar;
    }

    private static Car getGoalCar(Space space, ParkingLot goalState) {
      return goalState.getCarBySpace(space);
    }

    private static void moveCarToEmptySpace(Car car, ParkingLot currentState, List<Move> moves) {
      Space space = currentState.getSpaceByCar(car);
      Space emptySpace = currentState.getEmptySpace();
      currentState.getCarSpaceBiMap().put(emptySpace, car);
      currentState.getCarSpaceBiMap().put(space, Car.noCar);
      moves.add(new Move(car));
    }

    private static boolean carIsInWrongSpace(Car car, ParkingLot currentState, ParkingLot goalState) {
      Space currentSpace = currentState.getSpaceByCar(car);
      return !goalState.getCarBySpace(currentSpace).equals(car);
    }

    //=========================================================================================




    // getters and setters
    public Space getEmptySpace() {
        return getSpaceByCar(Car.noCar);
    }

    private Car getCarBySpace(Space space) {
        return carSpaceBiMap.getValue(space);
    }

    private Space getSpaceByCar(Car car) {
        return carSpaceBiMap.getKey(car);
    }

    public BiMap<Space, Car> getCarSpaceBiMap() {
        return carSpaceBiMap;
    }
    public int getSize() {
      return carSpaceBiMap.getKeyToValueMap().size();
    }



    // override methods
    @Override
    public String toString() {
        return carSpaceBiMap.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingLot parkingLot = (ParkingLot) o;
        return this.carSpaceBiMap.equals(parkingLot.carSpaceBiMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(carSpaceBiMap);
    }
}
