FROM nginx:1.19.3-alpine-perl

# to be able to read env vars, see: https://itnext.io/nginx-docker-and-environment-variables-9753dfb5d41
# RUN apk add --no-cache nginx-mod-http-perl

RUN rm -rf /etc/nginx/conf.d/default.conf

COPY nginx.conf.template /etc/nginx/templates/nginx.conf.template
# COPY nginx.conf /etc/nginx/conf.d
# COPY default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# CMD ["/bin/sh", "-c", "exec nginx -g 'daemon off;';"]