package pl.edu.platinum.archiet.jchess3man.engine;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static pl.edu.platinum.archiet.jchess3man.engine.GameState.newGame;

/**
 * Created by Michał Krzysztof Feiler on 18.02.17.
 */
class VecMoveTest {
    @Test
    void after() throws
            VectorAdditionFailedException,
            NeedsToBePromotedException,
            CheckInitiatedThruMoatException,
            ImpossibleMoveException {
        Pos from = new Pos(1, 0);
        Pos to = new Pos(3, 0);
        Fig fsq = newGame.board.get(from);
        assertTrue(fsq.equals(new Fig.Pawn(Color.White)));
        Iterable<? extends Vector> vecs = fsq.vecs(from, to);
        assertTrue(vecs.iterator().hasNext());
        Vector vec = vecs.iterator().next();
        assertNotNull(vec);
        VecMove first = new VecMove(new BoundVec(from, vec), newGame);
        System.out.println("A");
        Fig realsq = newGame.board.get(1, 0);
        System.out.println(realsq.toString());
        System.out.println("H");
        GameState firstAfter = first.after();
        assertNotNull(firstAfter);
        System.out.println(firstAfter.board.string());
    }

    @Test
    void simpleGenNoPanic() throws NeedsToBePromotedException {
        DescMove first = new DescMove(
                new Pos(1, 0),
                new Pos(3, 0),
                newGame
        );
        AtomicReference<GameState> temp = new AtomicReference<>();
        final AtomicBoolean thereAreStates = new AtomicBoolean(false);
        List<IllegalMoveException> exceptions = first.generateAfters().peek(
                (DescMove.EitherStateOrIllMoveExcept either) -> {
                    if (!thereAreStates.get() && either.isState()) {
                        temp.set(either.state);
                        thereAreStates.set(true);
                    }
                }).flatMap(DescMove.EitherStateOrIllMoveExcept::flatMapException)
                .collect(Collectors.toList());
        if (thereAreStates.get())
            System.out.println("first.After " + temp.get().board.string());
        else exceptions.forEach(Throwable::printStackTrace);
        assertTrue(thereAreStates.get());
        /*
        assertThrows(DescMove.MultipleIllegalMoveExceptions.class,
                () -> {
                    Optional<GameState> sTemp = new DescMove(
                            new Pos(3, 0),
                            new Pos(4, 0),
                            temp.get()
                    ).generateAfters().filter();
                });

        } catch (DescMove.MultipleIllegalMoveExceptions illegalMoveExceptions) {
            for(final IllegalMoveException single : illegalMoveExceptions) {
                if(single instanceof ImpossibleMoveException) {
                    System.out.println(
                        "jeden"+((ImpossibleMoveException) single).impossibility.msg()
                    );
                }
            }
            throw illegalMoveExceptions;
        }
        */
        assertFalse((new DescMove(
                new Pos(3, 0),
                new Pos(4, 0),
                temp.get()
        )).generateAfters()
                .flatMap(DescMove.EitherStateOrIllMoveExcept::flatMapState)
                .peek(state -> System.out.print("SECOND SUCCEDED BADLY" + state.toString()))
                .findAny().isPresent());

        assertThrows(NullPointerException.class,
                () -> assertFalse((new DescMove(
                        new Pos(3, 8),
                        new Pos(4, 8),
                        temp.get()
                )).generateAfters()
                        .flatMap(DescMove
                                .EitherStateOrIllMoveExcept::flatMapState)
                        .peek(state -> System.out
                                .print("THIRD SUCCEDED BADLY" + state.toString()))
                        .findAny().isPresent()));

        thereAreStates.set(false);
        exceptions = (new DescMove(
                new Pos(1, 8),
                new Pos(3, 8),
                temp.get()
        ))
                .generateAfters().peek(
                        (DescMove.EitherStateOrIllMoveExcept either) -> {
                            if (!thereAreStates.get() && either.isState()) {
                                temp.set(either.state);
                                thereAreStates.set(true);
                            }
                        }).flatMap(DescMove.EitherStateOrIllMoveExcept::flatMapException)
                .collect(Collectors.toList());
        if (thereAreStates.get())
            System.out.println("fourth.After " + temp.get().board.string());
        else {
            System.out.println("printing exceptions for fourth: ");
            exceptions.forEach(exception -> System.out.println(
                    exception instanceof ImpossibleMoveException
                            ? ((ImpossibleMoveException) exception).impossibility.msg()
                            : ""
            ));
            exceptions.forEach(Throwable::printStackTrace);
        }
        assertTrue(thereAreStates.get());
    }

    @Test
    void pawnCrossCenterTest() throws NeedsToBePromotedException {
        int i = 0;
        int j = 0;
        DescMove move = new DescMove(
                new Pos(1, i * 8),
                new Pos(3, i * 8),
                newGame
        );
        AtomicReference<GameState> statePointer = new AtomicReference<>();
        final AtomicBoolean thereAreStates = new AtomicBoolean(false);
        List<IllegalMoveException> exceptions = move.generateAfters().peek(
                (DescMove.EitherStateOrIllMoveExcept either) -> {
                    if (!thereAreStates.get() && either.isState()) {
                        statePointer.set(either.state);
                        thereAreStates.set(true);
                    }
                }).flatMap(DescMove.EitherStateOrIllMoveExcept::flatMapException)
                .collect(Collectors.toList());
        if (!thereAreStates.get()) exceptions.forEach(Throwable::printStackTrace);
        assertTrue(thereAreStates.get());

        for (i = 1; i < 3; i++) {
            thereAreStates.set(false);
            move = new DescMove(
                    new Pos(1, i * 8), new Pos(3, i * 8), statePointer.get());
            exceptions = move.generateAfters().peek(
                    (DescMove.EitherStateOrIllMoveExcept either) -> {
                        if (!thereAreStates.get() && either.isState()) {
                            statePointer.set(either.state);
                            thereAreStates.set(true);
                        }
                    }).flatMap(DescMove.EitherStateOrIllMoveExcept::flatMapException)
                    .collect(Collectors.toList());
            if (!thereAreStates.get()) {
                System.out.println("Unexpected error (1st loop, i=" + i + "): ");
                exceptions.forEach(Throwable::printStackTrace);
            }
            assertTrue(thereAreStates.get());
        }
        System.out.println("State after 3 moves: " + statePointer.get().board.string());
        for (i = 3; i < 5; i++) {
            for (j = 0; j < 3; j++) {
                thereAreStates.set(false);
                move = new DescMove(
                        new Pos(i, j * 8), new Pos(i + 1, j * 8), statePointer.get());
                exceptions = move.generateAfters().peek(
                        either -> {
                            if (!thereAreStates.get() && either.isState()) {
                                statePointer.set(either.state);
                                thereAreStates.set(true);
                            }
                        }).flatMap(DescMove.EitherStateOrIllMoveExcept::flatMapException)
                        .collect(Collectors.toList());
                if (!thereAreStates.get()) {
                    System.out.println("Unexpected error (2nd loop, i=" + i + " j=" + j + " ): ");
                    exceptions.forEach(Throwable::printStackTrace);
                }
                assertTrue(thereAreStates.get());
            }
        }
        System.out.println(
                "State after " + (i - 1) * 3 + " moves: " + statePointer.get().board.string());
        for (i = 0; i < 3; i++) {
            thereAreStates.set(false);
            move = new DescMove(
                    new Pos(5, i * 8), new Pos(5, (i * 8 + 12) % 24), statePointer.get());
            exceptions = move.generateAfters().peek(
                    (DescMove.EitherStateOrIllMoveExcept either) -> {
                        if (!thereAreStates.get() && either.isState()) {
                            statePointer.set(either.state);
                            thereAreStates.set(true);
                        }
                    }).flatMap(DescMove.EitherStateOrIllMoveExcept::flatMapException)
                    .collect(Collectors.toList());
            if (!thereAreStates.get()) {
                System.out.println("Unexpected error (3st loop, i=" + i + "): ");
                exceptions.forEach(exception -> System.out.println(
                        exception instanceof ImpossibleMoveException
                                ? ((ImpossibleMoveException) exception).impossibility.msg()
                                : ""
                ));
                exceptions.forEach(Throwable::printStackTrace);
            }
            assertTrue(thereAreStates.get());
        }
        System.out.println("State after another 3 moves: \n" + statePointer.get().board.string());
    }
}