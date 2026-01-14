# Scala ML Project
A machine learning project that predicts passenger survival on the Titanic using Scala. 
Built for a university Data Mining course.

## Project Overview
This project implements two different prediction models:
1. **Main Model**: A comprehensive machine learning model using multiple features
2. **Simple Baseline Model**: A gender-based model assuming "all women survived, all men perished"

## Project Structure
  ├── src/main/scala/
  
  │ ├── CreatePrediction.scala # Main prediction model
  
  │ ├── CreatePredictionSimple.scala # Simple baseline model
  
  │ └── Utils.scala # Helper functions
  
  ├── predictions/
  
  │ ├── TitanicPrediction.csv # Main model predictions
  
  │ ├── TitanicPrediction.txt # Main model predictions (text)
  
  │ ├── TitanicSimplePrediction.csv # Baseline model predictions
  
  │ └── TitanicSimplePrediction.txt # Baseline model predictions (text)
  
  ├── build.sbt # Scala build configuration
  
  ├── project/
  
  │ └── build.properties # SBT version
  
  └── README.md # This file

  
## Running the Model
### Prerequisites
- Java JDK 8 or higher
- Scala 2.13.x
- sbt (Scala Build Tool)

### Installation

1. **Clone the repository**
```bash
    git clone https://github.com/eladkilic/titanic-prediction.git
    cd titanic-prediction
````
2. **Run the project**
   ````bash
    sbt
    > run
   ````
