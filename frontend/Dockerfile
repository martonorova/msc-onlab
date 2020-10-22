FROM node:12

WORKDIR /code

ENV PATH /code/node_modules/.bin:$PATH


COPY package.json ./
COPY package-lock.json ./
RUN npm install
# RUN npm install react-scripts@3.4.3 -g

COPY . .

EXPOSE 5000

# CMD [ "npm", "run", "start"]
CMD [ "npm", "start"]