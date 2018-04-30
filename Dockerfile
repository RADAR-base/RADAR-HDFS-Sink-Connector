# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM openjdk:8-alpine as builder

RUN mkdir /code
WORKDIR /code
COPY ./gradle/ /code/gradle
COPY ./build.gradle ./gradle.properties ./gradlew ./settings.gradle /code/

RUN ./gradlew --no-daemon downloadDependencies

COPY ./src/ /code/src

RUN ./gradlew --no-daemon jar

FROM confluentinc/cp-kafka-connect:3.2.1

MAINTAINER Nivethika M <nivethika@thehyve.nl> , Joris B <joris@thehyve.nl>

LABEL description="RADAR-CNS Backend- HDFS Sink Connector"

COPY --from=builder /code/build/libs/*.jar /etc/kafka-connect/jars/

# Load topics validator
COPY ./docker/kafka_status.sh /home/kafka_status.sh

# Load modified launcher
COPY ./docker/launch /etc/confluent/docker/launch