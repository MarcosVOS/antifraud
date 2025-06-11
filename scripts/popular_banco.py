import requests
from faker import Faker

fake = Faker('pt_BR')

BASE_URL = "http://localhost:8080/customers"

def gerar_cliente():
    return {
        "name": fake.name(),
        "cpf": fake.cpf(),
        "email": fake.email(),
        "phone": fake.msisdn()[0:11],
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

def criar_clientes(qtd=5):
    for _ in range(qtd):
        cliente = gerar_cliente()
        response = requests.post(BASE_URL, json=cliente)
        if response.status_code == 201:
            print(f"✅ Cliente criado: {cliente['name']} | ID: {response.json()['id']}")
        else:
            print(f"❌ Erro ao criar {cliente['name']}: {response.status_code} - {response.text}")

if __name__ == "__main__":
    criar_clientes(100) 
