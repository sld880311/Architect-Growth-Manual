<!-- TOC -->

- [StampedLock](#stampedlock)
  - [使用场景](#使用场景)
  - [乐观读的原理](#乐观读的原理)

<!-- /TOC -->
# StampedLock

读写锁的改进。

| 锁 | 并发度 |
|----|--------|
|  ReentrantLock  |    读读互斥、读写互斥、写写互斥    |
|  ReentrantReadWriteLock  |   读读不互斥、读写互斥、写写互斥     |
|  StampedLock  |    读读不互斥、读写不互斥、写写互斥    |

## 使用场景

## 乐观读的原理

