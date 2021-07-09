FROM centos:7 AS MAVEN_BUILD

#installing java-openjdk-11
RUN yum install -y install java-11-openjdk
RUN yum install -y install java-11-openjdk-devel
RUN yum install -y epel-release
RUN echo java -version

#installing maven-3.8.1
WORKDIR /opt/
RUN curl https://downloads.apache.org/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz --output apache-maven-3.8.1-bin.tar.gz --insecure
RUN tar -xvf apache-maven-3.8.1-bin.tar.gz
RUN mv apache-maven-3.8.1 maven
COPY maven.sh /etc/profile.d/maven.sh
RUN chmod +x /etc/profile.d/maven.sh
RUN source /etc/profile.d/maven.sh

#Manupulating Application
COPY target/deid-3.0.19-SNAPSHOT-jar-with-dependencies.jar /opt/deid.jar
COPY javacmd.sh /opt/javacmd.sh
RUN chmod a+x /opt/javacmd.sh

#setting entrypoint
COPY entrypoint.sh  /entrypoint.sh
#ENTRYPOINT [ "/entrypoint.sh" ]

