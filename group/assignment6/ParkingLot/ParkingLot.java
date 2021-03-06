package assignment6.ParkingLot;

import assignment6.BiMap.BiMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Represents a parking lot that can have spaces in which cars can be parked.
 */
public class ParkingLot {

  private BiMap<Space,Car> carSpaceBiMap;
  
  /**
   * @param spaceToCar space to car map, full only with unique cars and spaces.
   */
  public ParkingLot(HashMap<Space,Car> spaceToCar) {
    carSpaceBiMap = new BiMap<>(spaceToCar);
  }

  public BiMap<Space, Car> getCarSpaceBiMap() {
    return carSpaceBiMap;
  }

  private Car getCarBySpace(Space space) {
    return carSpaceBiMap.getValue(space);
  }

  private Space getSpaceByCar(Car car) {
    return carSpaceBiMap.getKey(car);
  }

  public Space getEmptySpace() {
    return getSpaceByCar(Car.noCar);
  }
  
  public int getSize() {
    return carSpaceBiMap.getKeyToValueMap().size();
  }

  @Override
  public String toString() {
    return carSpaceBiMap.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ParkingLot parkingLot = (ParkingLot) o;
    return this.carSpaceBiMap.equals(parkingLot.carSpaceBiMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(carSpaceBiMap);
  }

  // ============================== Solution to challenge 1 and 2 ==============================

  /**
   * Finds a list of moves to rearrange cars from an initial state to a goal state.
   * This method assumes there is exactly one empty space.
   * @param goalState a rearrangement of the initial ParkingLot
   * @return a list of moves
   */
  public List<Move> getCarMoves(ParkingLot goalState) {
    if (!isRearrangementOfParkingLot(goalState)) {
      throw new IllegalArgumentException(
          "The goal parking lot state has to be a rearrangement of the initial parking lot state.");
    }

    List<Move> moves = new ArrayList<>();
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

  // ================================== Solution to challenge 4 ==================================

  /**
   * Gets all possible unique sequences of moves of cars in a parking lot.
   * Each sequence will not contain the same parking lot configuration more than once.
   * @param goalState a rearrangement of the initial ParkingLot
   */
  public List<List<DetailedMove>> getAllPossibleRearrangements(ParkingLot goalState) {
    List<List<DetailedMove>> allMoves = new ArrayList<>();
    List<DetailedMove> currentMoves = new ArrayList<>();
    List<ParkingLot> previousConfigurations = new ArrayList<>();
    HashMap<Space,Car> currentParkingLot = new HashMap<>(carSpaceBiMap.getKeyToValueMap());
    allRearrangementsRecursive(allMoves, currentMoves, previousConfigurations, goalState);
    carSpaceBiMap = new BiMap<>(currentParkingLot);
    return allMoves;
  }

  private void allRearrangementsRecursive(List<List<DetailedMove>> allMoves,
      List<DetailedMove> currentMoves,
      List<ParkingLot> previousConfigurations,
      ParkingLot goalState) {
    if (this.equals(goalState)) {
      allMoves.add(new ArrayList<>(currentMoves));
      return;
    }

    if (previousConfigurations.contains(this)) {
      return;
    }

    for (Car car : carSpaceBiMap.getValueSet()) {
      if (car.equals(Car.noCar)) {
        continue;
      }
      ParkingLot currentConfiguration = new ParkingLot(carSpaceBiMap.getKeyToValueMap());

      previousConfigurations.add(currentConfiguration);
      moveCarToEmptySpace(car, currentMoves);
      allRearrangementsRecursive(allMoves, currentMoves, previousConfigurations, goalState);
      moveCarToEmptySpace(car);
      currentMoves.remove(currentMoves.size() - 1);
      previousConfigurations.remove(currentConfiguration);
    }
  }

  public void makeMoves(List<DetailedMove> carMoves) {
    for (DetailedMove carMove : carMoves) {
      makeMove(carMove.getCar(), carMove.getFrom(), carMove.getTo());
      makeMove(Car.noCar, null, carMove.getFrom());
    }
  }

  // move with updating a List<CarMove> with the new car move
  private void moveCarToEmptySpace(Car car, List<DetailedMove> carMoveList) {
    Space toBeFilledEmptySpace = getEmptySpace();
    Space toBeEmptyFilledSpace = getSpaceByCar(car);

    DetailedMove carMove = makeMove(car,toBeEmptyFilledSpace,toBeFilledEmptySpace);
    if(carMoveList != null) {
      carMoveList.add(carMove);
    }
    makeMove(Car.noCar,null, toBeEmptyFilledSpace);
  }
  // move without updating a List<CarMove> with the new car move
  private boolean isRearrangementOfParkingLot(ParkingLot another) {
    return another.carSpaceBiMap.getKeySet().equals(carSpaceBiMap.getKeySet())
        && another.carSpaceBiMap.getValueSet().equals(carSpaceBiMap.getValueSet()) ;
  }

  private DetailedMove makeMove(Car car, Space from, Space to) {
    carSpaceBiMap.put(to, car);
    if (!car.equals(Car.noCar)) {
      return new DetailedMove(car,from,to);
    }
    return null;
  }

  private void moveCarToEmptySpace(Car car) {
    moveCarToEmptySpace(car,null);
  }
}
