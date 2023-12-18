# Bored Command

Bored Command is a command-line application that aims to provide users with interesting activities to combat boredom. The project is implemented in Scala, leveraging the Cats Effect library for asynchronous programming and Doobie for database interactions. It also integrates with the Bored API to fetch and suggest activities.

## Requirements
- Java (JDK 8 or higher)
- sbt (for building jar-file)

## Building the Project
Build the project using sbt:
    ```
    sbt assembly
    ```
Alternatively, you can find an already prepared jar file in the root of the repository 

## Running the application
Once you have built the project, you can run it using the following command:
    ```
    java -jar boredExec.jar
    ```

## Instructions for use
This CLI offers 4 interaction options:
1. Generate a new activity 
2. Show a list of all your favourite activities 
3. Remove from favourites
4. Brief help on the format of activities (especially useful for generating activities)

## Features

1. **Generate New Activity:**
   - Users can generate a new activity based on various parameters such as activity type, number of participants, price range, and accessibility.

2. **List Your Favorites:**
   - Users can view a list of their favorite activities and delete annoying ones.
