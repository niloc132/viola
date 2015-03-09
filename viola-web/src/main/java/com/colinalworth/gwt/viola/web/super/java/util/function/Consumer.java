/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package java.util.function;

@FunctionalInterface
public interface Consumer<T> {

  void accept(T t);

  default Consumer<T> andThen(Consumer<? super T> after) {
    return (T t) -> {
      accept(t);
      after.accept(t);
    };
  }
}
