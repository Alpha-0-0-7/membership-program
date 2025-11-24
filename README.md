# Membership Program

This project is a complete, production-quality Low-Level Design (LLD) implementation of the **Membership Program**.
It includes an interactive CLI, a background scheduler for subscription expiry, JUnit 5 tests, and a clear, maintainable object-oriented architecture.

---

## Dry Run & CLI Demo

A complete interactive CLI session (covering subscription creation, upgrade/downgrade, cancellation, evaluation, expiry, and validation) is available in:
See: [docs/dry-run-log.txt](docs/dry-run-log.txt)

---

## Project Overview

The membership system includes:

* Configurable membership plans (Monthly, Yearly)
* Multiple tiers (Silver, Gold, Platinum)
* Tier-specific benefits (discounts, early access, priority support)
* Subscription lifecycle management:

    * Create subscription
    * Upgrade or downgrade tier
    * Cancel subscription
    * Auto-expire subscriptions based on end date
* Tier evaluation logic
* Repository pattern with in-memory storage
* Background scheduler to auto-expire subscriptions
* Interactive CLI for testing and demonstration
* Complete JUnit test suite

---

## Features

### Membership Plans and Tiers

* Plans include Monthly and Yearly durations.
* Tiers include Silver, Gold, and Platinum with configurable benefits.
* Benefits supported:

    * Free delivery
    * Discount percentage
    * Early access
    * Priority support

### Subscription Management

* Subscribe users to a plan and tier.
* Upgrade/downgrade subscription tier.
* Cancel an active subscription.
* Automatically expire subscriptions when their expiration time passes.

### Tier Evaluation Engine

* Evaluates and assigns a new tier based on metrics such as:

    * Total order count
    * Total order value

### Background Scheduler

The system includes a `ScheduledExecutorService` that calls:

`expireDueSubscriptions()`

every 30 seconds to automatically expire subscriptions.

### Interactive CLI

A menu-driven CLI allows you to:

1. List all membership plans
2. Create a subscription
3. List subscriptions for a user
4. Upgrade or downgrade a subscription
5. Cancel a subscription
6. Evaluate a subscription based on metrics
7. Exit the program

### Testing

JUnit 5 tests validate:

* Subscription creation
* Tier evaluation logic
* Subscription expiry logic

---

## Building the Project

### Prerequisites

* Java 17 installed
* Maven installed

To verify:

```
java -version
mvn -version
```

### Build Command

Run the following from the project root:

```
mvn clean package
```

This generates the runnable JAR:

```
target/membership-program-1.0.0.jar
```

---

## Running the Application

To run the interactive CLI program:

```
java -jar target/membership-program-1.0.0.jar
```

On startup, the application:

1. Loads demo plans and a default demo user (`user-1`)
2. Starts the background scheduler (auto-expiry)
3. Launches the interactive CLI menu

---

## Using the Interactive CLI

After launching, you will see:

```
=== FirstClub CLI ===
1) List plans
2) Create subscription
3) List user's subscriptions
4) Upgrade/downgrade subscription
5) Cancel subscription
6) Evaluate subscription (apply metrics)
7) Exit
Choose:
```

Use `user-1` for testing.

Example values:

* Plan IDs: `MONTHLY`, `YEARLY`
* Tier IDs: `SILVER`, `GOLD`, `PLATINUM`
* Metrics for evaluation:

    * ordersCount
    * orderValue

---

## Running Tests

Run the full test suite:

```
mvn test
```

Tests cover:

* Subscription creation
* Tier evaluation
* Subscription expiry

---

## Scheduler Details

Implemented using Java's `ScheduledExecutorService`:

* Initial delay: 10 seconds
* Repeated every: 30 seconds
* Background thread automatically expires any subscription whose expiration time has passed

The scheduler is gracefully shut down when the application exits.

---

## Extensibility

The codebase is structured to allow:

* Adding new plan types or benefits
* Plugging in an ML-based or rules-based tier evaluator
* Replacing in-memory repositories with MySQL/PostgreSQL
* Migrating CLI to REST API without changing core business logic
* Adding real authentication and user accounts
