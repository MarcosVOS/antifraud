import requests
from faker import Faker
from datetime import datetime, timedelta
import random
import time

fake = Faker("pt_BR")

BASE_URL = "http://localhost:8080"

def gerar_cliente():
    return {
        "name": fake.name(),
        "cpf": fake.cpf(),
        "email": fake.email(),
        "phone": fake.msisdn()[:11],
        "dateOfBirth": str(fake.date_of_birth(minimum_age=18, maximum_age=65)),
        "address": {
            "street": fake.street_name(),
            "number": str(fake.building_number()),
            "city": fake.city(),
            "state": fake.estado_sigla(),
            "zipCode": fake.postcode()
        },
        "password": "senha123"
    }

def gerar_conta(customer_id):
    return {
        "numeroConta": fake.numerify(text="######"),
        "agencia": fake.numerify(text="000#"),
        "saldo": round(random.uniform(100.0, 10000.0), 2),
        "tipoConta": random.choice(["CORRENTE", "POUPANCA"]),
        "status": "ATIVA",
        "customerId": customer_id
    }

def gerar_transacao(contas):
    tipo = random.choice(["DEPOSITO", "SAQUE", "TRANSFERENCIA", "PAGAMENTO"])
    conta_origem = random.choice(contas)
    conta_destino = random.choice(contas)

    if tipo == "DEPOSITO":
        return {
            "tipo": "DEPOSITO",
            "valor": round(random.uniform(50.0, 2000.0), 2),
            "dataHora": datetime.now().isoformat(),
            "descricao": "Depósito automático",
            "contaDeOrigem": None,
            "contaDeDestino": conta_destino["id"]
        }
    elif tipo == "SAQUE":
        return {
            "tipo": "SAQUE",
            "valor": round(random.uniform(20.0, 1000.0), 2),
            "dataHora": datetime.now().isoformat(),
            "descricao": "Saque automático",
            "contaDeOrigem": conta_origem["id"],
            "contaDeDestino": None
        }
    elif tipo == "TRANSFERENCIA":
        if conta_origem["id"] == conta_destino["id"]:
            return None  # evita mesma conta
        return {
            "tipo": "TRANSFERENCIA",
            "valor": round(random.uniform(10.0, 1000.0), 2),
            "dataHora": datetime.now().isoformat(),
            "descricao": "Transferência entre contas",
            "contaDeOrigem": conta_origem["id"],
            "contaDeDestino": conta_destino["id"]
        }
    elif tipo == "PAGAMENTO":
        return {
            "tipo": "PAGAMENTO",
            "valor": round(random.uniform(30.0, 500.0), 2),
            "dataHora": datetime.now().isoformat(),
            "descricao": "Pagamento de boleto",
            "contaDeOrigem": conta_origem["id"],
            "contaDeDestino": None
        }

def criar_clientes_com_contas_e_transacoes(qtd_clientes=5, contas_por_cliente=2, transacoes=10):
    contas = []

    for _ in range(qtd_clientes):
        # Criar cliente
        cliente = gerar_cliente()
        response = requests.post(f"{BASE_URL}/customers", json=cliente)
        if response.status_code != 201:
            print(f"Erro ao criar cliente: {response.text}")
            continue
        cliente_data = response.json()
        customer_id = cliente_data["id"]
        print(f"✅ Cliente criado: {cliente_data['name']} ({customer_id})")

        # Criar contas
        for _ in range(contas_por_cliente):
            conta = gerar_conta(customer_id)
            res = requests.post(f"{BASE_URL}/accounts/newaccount", json=conta)
            if res.status_code == 201:
                conta_criada = res.json()
                contas.append(conta_criada)
                print(f"  ✅ Conta criada: {conta_criada['numeroConta']} (Saldo: {conta_criada['saldo']})")
            else:
                print(f"  ❌ Erro ao criar conta: {res.text}")

    # Criar transações
    for _ in range(transacoes):
        transacao = None
        while transacao is None:
            transacao = gerar_transacao(contas)

        res = requests.post(f"{BASE_URL}/transactions", json=transacao)
        if res.status_code == 201:
            print(f"✅ Transação criada: {transacao['tipo']} de R${transacao['valor']}")
        else:
            print(f"❌ Erro ao criar transação: {res.status_code} - {res.text}")
        time.sleep(0.3)  # evita sobrecarga

# Executar
if __name__ == "__main__":
    criar_clientes_com_contas_e_transacoes(
        qtd_clientes=100,
        contas_por_cliente=2,
        transacoes=100
    )
