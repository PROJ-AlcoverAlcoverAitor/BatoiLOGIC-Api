import json
import os
import requests
from flask import Flask, request, jsonify, g
import bcrypt
import jwt
from datetime import datetime, timedelta, timezone
from functools import wraps
os.system('cls' if os.name == 'nt' else 'clear')
SECRET_KEY = 'hgagr513º1lkaur*?karu>>126#@!$%&/()=+'
TOKEN_EXPIRATION_HOURS = 900
app = Flask(__name__)

# Archivos de persistencia
CLIENTES_FILE = 'clientes.json'
EMPLEADOS_FILE = 'empleados.json'
ADMINS_FILE = 'admins.json'
PEDIDOS_FILE = 'pedidos.json'

# Blacklist tokens
tokens_revocados = set()


# --- UTILIDADES DE PERSISTENCIA ---

def gestionar_json(archivo, datos=None, lectura=True):
    if lectura:
        if not os.path.exists(archivo): return []
        with open(archivo, 'r', encoding='utf-8') as f:
            try: return json.load(f)
            except: return []
    else:
        with open(archivo, 'w', encoding='utf-8') as f:
            json.dump(datos, f, indent=4, ensure_ascii=False)

def obtener_coords(direccion):
    url = "https://nominatim.openstreetmap.org/search"
    params = {'q': direccion, 'format': 'json', 'limit': 1}
    headers = {'User-Agent': 'FlutterApp_2026/1.0'}
    try:
        resp = requests.get(url, params=params, headers=headers, timeout=5)
        if resp.status_code == 200 and resp.json():
            d = resp.json()[0]
            return {"lat": float(d["lat"]), "lng": float(d["lon"])}
    except Exception as e:
        print(f"Error geocodificando: {e}")
    return {"lat": 0.0, "lng": 0.0}


# --- UTILIDADES JWT ---

def generar_token(user_id, email, rol):
    payload = {
        'sub': str(user_id),
        'email': email,
        'rol': rol,
        'exp': datetime.now(timezone.utc) + timedelta(hours=TOKEN_EXPIRATION_HOURS),
        'iat': datetime.now(timezone.utc)
    }
    return jwt.encode(payload, SECRET_KEY, algorithm='HS256')


def token_requerido(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth_header = request.headers.get('Authorization')
        if not auth_header or not auth_header.startswith('Bearer '):
            return jsonify({"error": "Token no proporcionado"}), 401

        token = auth_header.split(' ')[1]

        if token in tokens_revocados:
            return jsonify({"error": "Token revocado"}), 401

        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=['HS256'])
            g.usuario_id = int(payload['sub'])
            g.email = payload['email']
            g.rol = payload['rol']
        except jwt.ExpiredSignatureError:
            return jsonify({"error": "Token expirado"}), 401
        except jwt.InvalidTokenError as e:
            return jsonify({"error": f"Token inválido: {str(e)}"}), 401
        except Exception as e:
            return jsonify({"error": f"Error inesperado: {str(e)}"}), 500

        return f(*args, **kwargs)
    return decorated


# --- SETUP INICIAL (solo si no hay admins) ---

@app.route('/setup', methods=['POST'])
def setup():
    """Crea el primer admin. Solo funciona si admins.json está vacío."""
    admins = gestionar_json(ADMINS_FILE)
    if admins:
        return jsonify({"error": "Setup ya realizado"}), 403

    password = request.args.get('password')
    nombre = request.args.get('nombre', 'Admin')
    email = request.args.get('email', 'admin@batoilogic.com')

    if not password:
        return jsonify({"error": "Falta password"}), 400

    hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

    admin = {
        "id": 1,
        "nombre": nombre,
        "email": email,
        "password": hashed,
        "rol": "admin"
    }

    gestionar_json(ADMINS_FILE, [admin], lectura=False)
    return jsonify({"mensaje": "Admin creado correctamente", "email": email}), 201


# --- AUTENTICACIÓN ---

@app.route('/login', methods=['POST'])
def login():
    email = request.args.get('email')
    password = request.args.get('password')

    if not all([email, password]):
        return jsonify({"error": "Faltan datos (email o password)"}), 400

    # Buscar en admins primero, luego empleados, luego clientes
    for archivo, rol in [(ADMINS_FILE, 'admin'), (EMPLEADOS_FILE, 'empleado'), (CLIENTES_FILE, 'cliente')]:
        usuarios = gestionar_json(archivo)
        usuario = next((u for u in usuarios if u['email'] == email), None)
        if usuario:
            if not bcrypt.checkpw(password.encode('utf-8'), usuario['password'].encode('utf-8')):
                return jsonify({"error": "Contraseña incorrecta"}), 401

            token = generar_token(usuario['id'], usuario['email'], rol)
            print({
                "id": usuario['id'],
                "nombre": usuario['nombre'],
                "email": usuario['email'],
                "rol": rol,
                "token": token
            })
            return jsonify({
                "id": usuario['id'],
                "nombre": usuario['nombre'],
                "email": usuario['email'],
                "rol": rol,
                "token": token
            }), 200

    return jsonify({"error": "Usuario no encontrado"}), 404


@app.route('/logout', methods=['POST'])
@token_requerido
def logout():
    token = request.headers.get('Authorization').split(' ')[1]
    tokens_revocados.add(token)
    return jsonify({"mensaje": "Sesión cerrada correctamente"}), 200


@app.route('/refresh', methods=['POST'])
@token_requerido
def refresh():
    """Renueva el token si aún es válido."""
    # Revocar el token anterior
    token_viejo = request.headers.get('Authorization').split(' ')[1]
    tokens_revocados.add(token_viejo)

    # Generar nuevo token con los mismos datos
    nuevo_token = generar_token(g.usuario_id, g.email, g.rol)

    return jsonify({"token": nuevo_token}), 200


# --- REGISTRO ---

@app.route('/register', methods=['POST'])
def register():
    nombre = request.args.get('nombre')
    email = request.args.get('email')
    password = request.args.get('password')
    rol_solicitado = request.args.get('rol', 'cliente')

    if not all([nombre, email, password]):
        return jsonify({"error": "Faltan datos (nombre, email o password)"}), 400

    # Solo admin puede crear empleados y admins
    if rol_solicitado in ['empleado', 'admin']:
        auth_header = request.headers.get('Authorization')
        if not auth_header or not auth_header.startswiEmplth('Bearer '):
            return jsonify({"error": "Se requiere token de admin"}), 401
        try:
            token = auth_header.split(' ')[1]
            payload = jwt.decode(token, SECRET_KEY, algorithms=['HS256'])
            if payload.get('rol') != 'admin':
                return jsonify({"error": "Solo un admin puede crear empleados"}), 403
        except jwt.InvalidTokenError:
            return jsonify({"error": "Token inválido"}), 401

    # Seleccionar archivo según rol
    if rol_solicitado == 'admin':
        archivo = ADMINS_FILE
    elif rol_solicitado == 'empleado':
        archivo = EMPLEADOS_FILE
    else:
        archivo = CLIENTES_FILE

    usuarios = gestionar_json(archivo)

    # Comprobar email duplicado en todos los archivos
    for f in [ADMINS_FILE, EMPLEADOS_FILE, CLIENTES_FILE]:
        if any(u['email'] == email for u in gestionar_json(f)):
            return jsonify({"error": "El email ya está registrado"}), 409

    nuevo_id = max([u['id'] for u in usuarios]) + 1 if usuarios else 1
    hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

    nuevo_usuario = {
        "id": nuevo_id,
        "nombre": nombre,
        "email": email,
        "password": hashed,
        "rol": rol_solicitado
    }

    usuarios.append(nuevo_usuario)
    gestionar_json(archivo, usuarios, lectura=False)

    return jsonify({"id": nuevo_id, "nombre": nombre, "email": email, "rol": rol_solicitado}), 201


# --- ENDPOINTS DE PEDIDOS ---

@app.route('/pedidos', methods=['POST'])
@token_requerido
def add_pedido():
    if g.rol not in ['admin', 'empleado']:
        return jsonify({"error": "No autorizado"}), 403

    cliente = request.args.get('nombre')
    direccion = request.args.get('direccion')
    fecha = request.args.get('fecha')
    telefono = request.args.get('telefono')
    repartidor_id = request.args.get('repartidor_id', type=int)

    if not all([cliente, direccion, fecha, telefono, repartidor_id]):
        return jsonify({"error": "Faltan datos"}), 400

    pedidos = gestionar_json(PEDIDOS_FILE)
    nuevo_id = max([p['id'] for p in pedidos]) + 1 if pedidos else 1

    coords = obtener_coords(direccion)

    nuevo_pedido = {
        "id": nuevo_id,
        "repartidor_id": repartidor_id,
        "cliente": cliente,
        "direccion": direccion,
        "fecha": fecha,
        "estado": "En ruta",
        "telefono": telefono,
        "coords": coords,
        "incidencia": None
    }

    pedidos.append(nuevo_pedido)
    gestionar_json(PEDIDOS_FILE, pedidos, lectura=False)
    return jsonify(nuevo_pedido), 201


@app.route('/pedidos', methods=['GET'])
@token_requerido
def get_pedidos():
    dia = request.args.get('dia')

    pedidos = gestionar_json(PEDIDOS_FILE)

    if g.rol == 'empleado':
        if not dia:
            return jsonify({"error": "Parámetro 'dia' es obligatorio"}), 400
        filtrados = [p for p in pedidos if p['fecha'] == dia and p.get('repartidor_id') == g.usuario_id]
    elif g.rol == 'admin':
        filtrados = [p for p in pedidos if p['fecha'] == dia] if dia else pedidos
    else:
        return jsonify({"error": "No autorizado"}), 403
    print({"day": dia, "comandas": filtrados})
    return jsonify({"day": dia, "comandas": filtrados})


@app.route('/pedidos/<int:pedido_id>', methods=['GET'])
@token_requerido
def get_pedido(pedido_id):
    pedidos = gestionar_json(PEDIDOS_FILE)
    pedido = next((p for p in pedidos if p['id'] == pedido_id), None)

    if not pedido:
        return jsonify({"error": "Pedido no encontrado"}), 404

    if g.rol == 'empleado' and pedido.get('repartidor_id') != g.usuario_id:
        return jsonify({"error": "No autorizado"}), 403

    return jsonify(pedido), 200


@app.route('/pedidos/<int:pedido_id>/estado', methods=['PATCH'])
@token_requerido
def update_estado(pedido_id):
    nuevo_estado = request.args.get('estado')
    estados_validos = ['En ruta', 'Entregada', 'No entregado', 'Pendiente de recoger']

    if not nuevo_estado or nuevo_estado not in estados_validos:
        return jsonify({"error": f"Estado inválido. Valores posibles: {estados_validos}"}), 400

    pedidos = gestionar_json(PEDIDOS_FILE)
    pedido = next((p for p in pedidos if p['id'] == pedido_id), None)

    if not pedido:
        return jsonify({"error": "Pedido no encontrado"}), 404

    if g.rol == 'empleado' and pedido.get('repartidor_id') != g.usuario_id:
        return jsonify({"error": "No autorizado"}), 403

    pedido['estado'] = nuevo_estado
    gestionar_json(PEDIDOS_FILE, pedidos, lectura=False)
    return jsonify(pedido), 200


@app.route('/pedidos/<int:pedido_id>/incidencia', methods=['PATCH'])
@token_requerido
def update_incidencia(pedido_id):
    incidencia = request.args.get('incidencia')

    pedidos = gestionar_json(PEDIDOS_FILE)
    pedido = next((p for p in pedidos if p['id'] == pedido_id), None)

    if not pedido:
        return jsonify({"error": "Pedido no encontrado"}), 404

    if g.rol == 'empleado' and pedido.get('repartidor_id') != g.usuario_id:
        return jsonify({"error": "No autorizado"}), 403

    pedido['incidencia'] = incidencia
    gestionar_json(PEDIDOS_FILE, pedidos, lectura=False)
    return jsonify(pedido), 200


# --- ENDPOINTS DE REPARTIDOR ---

@app.route('/repartidor/ubicacion', methods=['POST'])
@token_requerido
def update_ubicacion():
    if g.rol != 'empleado':
        return jsonify({"error": "Solo empleados pueden actualizar ubicación"}), 403

    lat = request.args.get('lat', type=float)
    lng = request.args.get('lng', type=float)

    if lat is None or lng is None:
        return jsonify({"error": "Faltan datos (lat o lng)"}), 400

    empleados = gestionar_json(EMPLEADOS_FILE)
    empleado = next((e for e in empleados if e['id'] == g.usuario_id), None)

    if not empleado:
        return jsonify({"error": "Empleado no encontrado"}), 404

    empleado['ubicacion'] = {
        "lat": lat,
        "lng": lng,
        "actualizado": datetime.now(timezone.utc).isoformat()
    }

    gestionar_json(EMPLEADOS_FILE, empleados, lectura=False)
    print({"mensaje": "Ubicación actualizada", "lat": lat, "lng": lng})
    return jsonify({"mensaje": "Ubicación actualizada", "lat": lat, "lng": lng}), 200


@app.route('/repartidores/ubicaciones', methods=['GET'])
@token_requerido
def get_ubicaciones():
    """Admin ve la ubicación de todos los repartidores en tiempo real."""
    if g.rol != 'admin':
        return jsonify({"error": "Solo admins pueden ver la flota"}), 403

    empleados = gestionar_json(EMPLEADOS_FILE)
    ubicaciones = [
        {
            "id": e['id'],
            "nombre": e['nombre'],
            "ubicacion": e.get('ubicacion', None)
        }
        for e in empleados if e.get('ubicacion')
    ]

    return jsonify({"repartidores": ubicaciones}), 200


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)