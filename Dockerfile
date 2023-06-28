FROM maven:3.6.3-openjdk-17-slim

WORKDIR /home/compiler

ADD entrypoint.sh entrypoint.sh
CMD [ "entrypoint.sh" ]
ENTRYPOINT [ "sh" ]
