package sk.neuromancer.Xune.network;

import com.github.quantranuk.protobuf.nio.serializer.IdSerializer;
import sk.neuromancer.Xune.proto.MessageProto;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;

public class Utils {

    public static IdSerializer getIdSerializer() {
        return IdSerializer.create(List.of(MessageProto.State.class, MessageProto.Action.class, MessageProto.Event.class, MessageProto.Connection.class));
    }


    /*
     * Taken from Java 23 sources, because of reasons.
     *
     *
     * Copyright (c) 2021, 2024, Oracle and/or its affiliates. All rights reserved.
     * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
     *
     * This code is free software; you can redistribute it and/or modify it
     * under the terms of the GNU General Public License version 2 only, as
     * published by the Free Software Foundation.  Oracle designates this
     * particular file as subject to the "Classpath" exception as provided
     * by Oracle in the LICENSE file that accompanied this code.
     *
     * This code is distributed in the hope that it will be useful, but WITHOUT
     * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
     * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
     * version 2 for more details (a copy is included in the LICENSE file that
     * accompanied this code).
     *
     * You should have received a copy of the GNU General Public License version
     * 2 along with this work; if not, write to the Free Software Foundation,
     * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
     *
     * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
     * or visit www.oracle.com if you need additional information or have any
     * questions.
     */
    public static final class Xoroshiro128PlusPlus implements RandomGenerator.LeapableGenerator {
        /*
         * Implementation Overview.
         *
         * This is an implementation of the xoroshiro128++ algorithm version 1.0,
         * written in 2019 by David Blackman and Sebastiano Vigna (vigna@acm.org).
         *
         * The jump operation moves the current generator forward by 2*64
         * steps; this has the same effect as calling nextLong() 2**64
         * times, but is much faster.  Similarly, the leap operation moves
         * the current generator forward by 2*96 steps; this has the same
         * effect as calling nextLong() 2**96 times, but is much faster.
         * The copy method may be used to make a copy of the current
         * generator.  Thus one may repeatedly and cumulatively copy and
         * jump to produce a sequence of generators whose states are well
         * spaced apart along the overall state cycle (indeed, the jumps()
         * and leaps() methods each produce a stream of such generators).
         * The generators can then be parceled out to other threads.
         *
         * File organization: First the non-public methods that constitute the
         * main algorithm, then the public methods.  Note that many methods are
         * defined by classes {@link AbstractJumpableGenerator} and {@link AbstractGenerator}.
         */

        /* ---------------- static fields ---------------- */

        /**
         * The first 32 bits of the golden ratio (1+sqrt(5))/2, forced to be odd.
         * Useful for producing good Weyl sequences or as an arbitrary nonzero odd
         * value.
         */
        public static final int GOLDEN_RATIO_32 = 0x9e3779b9;

        /**
         * The first 64 bits of the golden ratio (1+sqrt(5))/2, forced to be odd.
         * Useful for producing good Weyl sequences or as an arbitrary nonzero odd
         * value.
         */
        public static final long GOLDEN_RATIO_64 = 0x9e3779b97f4a7c15L;

        /**
         * The first 32 bits of the silver ratio 1+sqrt(2), forced to be odd. Useful
         * for producing good Weyl sequences or as an arbitrary nonzero odd value.
         */
        public static final int SILVER_RATIO_32 = 0x6A09E667;

        /**
         * The first 64 bits of the silver ratio 1+sqrt(2), forced to be odd. Useful
         * for producing good Weyl sequences or as an arbitrary nonzero odd value.
         */
        public static final long SILVER_RATIO_64 = 0x6A09E667F3BCC909L;

        /**
         * Group name.
         */
        private static final String GROUP = "Xoroshiro";

        /**
         * The seed generator for default constructors.
         */
        private static final AtomicLong defaultGen = new AtomicLong(System.nanoTime() + Thread.currentThread().threadId());

        /* ---------------- instance fields ---------------- */

        /**
         * The per-instance state.
         * At least one of the two fields x0 and x1 must be nonzero.
         */
        private long x0, x1;

        /* ---------------- constructors ---------------- */

        /**
         * Basic constructor that initializes all fields from parameters.
         * It then adjusts the field values if necessary to ensure that
         * all constraints on the values of fields are met.
         *
         * @param x0 first word of the initial state
         * @param x1 second word of the initial state
         */
        public Xoroshiro128PlusPlus(long x0, long x1) {
            this.x0 = x0;
            this.x1 = x1;
            // If x0 and x1 are both zero, we must choose nonzero values.
            if ((x0 | x1) == 0) {
                this.x0 = GOLDEN_RATIO_64;
                this.x1 = SILVER_RATIO_64;
            }
        }

        /**
         * Creates a new instance of {@link Xoroshiro128PlusPlus} using the
         * specified {@code long} value as the initial seed. Instances of
         * {@link Xoroshiro128PlusPlus} created with the same seed in the same
         * program generate identical sequences of values.
         *
         * @param seed the initial seed
         */
        public Xoroshiro128PlusPlus(long seed) {
            // Using a value with irregularly spaced 1-bits to xor the seed
            // argument tends to improve "pedestrian" seeds such as 0 or
            // other small integers.  We may as well use SILVER_RATIO_64.
            //
            // The x values are then filled in as if by a SplitMix PRNG with
            // GOLDEN_RATIO_64 as the gamma value and Stafford13 as the mixer.
            this(seed ^ SILVER_RATIO_64, seed + GOLDEN_RATIO_64);
        }

        /**
         * Creates a new instance of {@link Xoroshiro128PlusPlus} that is likely to
         * generate sequences of values that are statistically independent
         * of those of any other instances in the current program execution,
         * but may, and typically does, vary across program invocations.
         */
        public Xoroshiro128PlusPlus() {
            // Using GOLDEN_RATIO_64 here gives us a good Weyl sequence of values.
            this(defaultGen.getAndAdd(GOLDEN_RATIO_64));
        }

        /**
         * Creates a new instance of {@link Xoroshiro128PlusPlus} using the specified array of
         * initial seed bytes. Instances of {@link Xoroshiro128PlusPlus} created with the same
         * seed array in the same program execution generate identical sequences of values.
         *
         * @param seed the initial seed
         */
        public Xoroshiro128PlusPlus(byte[] seed) {
            // Convert the seed to 2 long values, which are not both zero.
            long[] data = new long[]{
                    ((long) seed[0]) << 8 | ((long) seed[1] & 0xff),
                    ((long) seed[2]) << 8 | ((long) seed[3] & 0xff)};
            long x0 = data[0], x1 = data[1];
            this.x0 = x0;
            this.x1 = x1;
        }

        public Xoroshiro128PlusPlus(long[] seed) {
            this(seed[0], seed[1]);
        }

        /* ---------------- public methods ---------------- */

        public Xoroshiro128PlusPlus copy() {
            return new Xoroshiro128PlusPlus(x0, x1);
        }

        public long[] getState() {
            return new long[]{x0, x1};
        }

        public void setState(long[] state) {
            x0 = state[0];
            x1 = state[1];
        }

        public void setState(long state0, long state1) {
            x0 = state0;
            x1 = state1;
        }

        /*
         * The following two comments are quoted from http://prng.di.unimi.it/xoroshiro128plusplus.c
         */

        /*
         * To the extent possible under law, the author has dedicated all copyright
         * and related and neighboring rights to this software to the public domain
         * worldwide. This software is distributed without any warranty.
         * <p>
         * See http://creativecommons.org/publicdomain/zero/1.0/.
         */

        /*
         * This is xoroshiro128++ 1.0, one of our all-purpose, rock-solid,
         * small-state generators. It is extremely (sub-ns) fast and it passes all
         * tests we are aware of, but its state space is large enough only for
         * mild parallelism.
         * <p>
         * For generating just floating-point numbers, xoroshiro128+ is even
         * faster (but it has a very mild bias, see notes in the comments).
         * <p>
         * The state must be seeded so that it is not everywhere zero. If you have
         * a 64-bit seed, we suggest to seed a splitmix64 generator and use its
         * output to fill s.
         */

        @Override
        public long nextLong() {
            final long s0 = x0;
            long s1 = x1;
            // Compute the result based on current state information
            // (this allows the computation to be overlapped with state update).
            final long result = Long.rotateLeft(s0 + s1, 17) + s0;  // "plusplus" scrambler

            s1 ^= s0;
            x0 = Long.rotateLeft(s0, 49) ^ s1 ^ (s1 << 21); // a, b
            x1 = Long.rotateLeft(s1, 28); // c

            return result;
        }

        @Override
        public double jumpDistance() {
            return 0x1.0p64;
        }

        @Override
        public double leapDistance() {
            return 0x1.0p96;
        }

        private static final long[] JUMP_TABLE = {0x2bd7a6a6e99c2ddcL, 0x0992ccaf6a6fca05L};

        private static final long[] LEAP_TABLE = {0x360fd5f2cf8d5d99L, 0x9c6e6877736c46e3L};

        @Override
        public void jump() {
            jumpAlgorithm(JUMP_TABLE);
        }

        @Override
        public void leap() {
            jumpAlgorithm(LEAP_TABLE);
        }

        private void jumpAlgorithm(long[] table) {
            long s0 = 0, s1 = 0;
            for (int i = 0; i < table.length; i++) {
                for (int b = 0; b < 64; b++) {
                    if ((table[i] & (1L << b)) != 0) {
                        s0 ^= x0;
                        s1 ^= x1;
                    }
                    nextLong();
                }
            }
            x0 = s0;
            x1 = s1;
        }
    }
}
