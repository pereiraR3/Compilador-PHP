#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="$ROOT_DIR/out"

read -r -p "Arquivo .php de entrada (ex: dados/correto.php): " ENTRADA
if [ -z "${ENTRADA}" ]; then
  echo "Entrada vazia. Abortando."
  exit 1
fi

read -r -p "Arquivo de saida (ex: saidas/codigo.objeto.txt) [enter para padrao]: " SAIDA
if [ -z "${SAIDA}" ]; then
  SAIDA="saidas/codigo.objeto.txt"
fi

ENTRADA_PATH="$ROOT_DIR/$ENTRADA"
SAIDA_PATH="$ROOT_DIR/$SAIDA"

if [ ! -f "$ENTRADA_PATH" ]; then
  echo "Arquivo de entrada nao encontrado: $ENTRADA_PATH"
  exit 1
fi

mkdir -p "$OUT_DIR"
mkdir -p "$(dirname "$SAIDA_PATH")"

javac -d "$OUT_DIR" $(find "$ROOT_DIR/src/main/java" -name "*.java")
rm -f "$SAIDA_PATH"
java -cp "$OUT_DIR" compilador.Main "$ENTRADA_PATH" "$SAIDA_PATH" >/dev/null

read -r -p "Executar agora a Maquina Hipotetica? (s/N): " EXECUTAR
if [[ "${EXECUTAR}" =~ ^[sS]$ ]]; then
  java -cp "$OUT_DIR" compilador.MaqHipo.MaquinaHipoteticaMain "$SAIDA_PATH"
fi
