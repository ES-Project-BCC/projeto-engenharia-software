# ==========================================
# Etapa 1: build da aplicacao Angular
# ==========================================
FROM node:22-alpine AS build

WORKDIR /app

# Copia so o package.json primeiro pra aproveitar o cache do Docker
COPY package*.json ./
RUN npm ci

# Copia o codigo e compila no modo producao
COPY . .
RUN npm run build -- --configuration production

# ==========================================
# Etapa 2: Nginx para servir os arquivos estaticos
# ==========================================
FROM nginx:alpine

# Remove a configuracao padrao do nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copia a nossa configuracao customizada
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia os arquivos compilados pelo Angular (dist/frontend/browser)
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
