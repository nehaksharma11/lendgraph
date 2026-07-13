# LendGraph 💸🕸️

> Peer-to-peer micro-lending platform powered by **Neo4j graph database**.  
> Trust scores and fraud detection through graph relationship traversal.

Built for **HackHazards '26** — Neo4j Track + Render Track

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Database | Neo4j AuraDB (graph) |
| Security | Spring Security + JWT |
| Docs | Swagger / OpenAPI 3 |
| Deploy | Docker + Render |

---

## Quick Start

### 1. Set up Neo4j AuraDB (free)
1. Go to [neo4j.com/cloud/aura](https://neo4j.com/cloud/aura)
2. Create a free AuraDB instance
3. Download credentials (URI, username, password)

### 2. Set environment variables
```bash
export NEO4J_URI=neo4j+s://xxxxxxxx.databases.neo4j.io
export NEO4J_USERNAME=neo4j
export NEO4J_PASSWORD=your-password
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

### 3. Run locally
```bash
cd lendgraph
mvn spring-boot:run
```

### 4. Access Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## API Endpoints

### 🔐 Auth
```
POST /api/auth/register    Register new user
POST /api/auth/login       Login, get JWT token
```

### 👤 Users
```
GET  /api/users/me         My profile
GET  /api/users/{id}       Public profile
PATCH /api/users/me/kyc   Verify KYC
```

### 💰 Loans
```
POST   /api/loans              Create loan offer
GET    /api/loans/my           All my loans
GET    /api/loans/as-lender    Loans I gave
GET    /api/loans/as-borrower  Loans I received
GET    /api/loans/{id}         Loan details
PATCH  /api/loans/{id}/accept  Accept a loan
PATCH  /api/loans/{id}/reject  Reject a loan
GET    /api/loans/{id}/fraud-risk  Graph fraud analysis ⭐
GET    /api/loans/overdue      All overdue loans
```

### 💳 Repayments
```
POST /api/loans/{id}/repay        Make a repayment
GET  /api/loans/{id}/repayments   View repayments for loan
GET  /api/repayments/my           My repayment history
```

### 🧠 Trust & Graph
```
GET /api/trust/score/me      My trust score + badges
GET /api/trust/score/{id}    Another user's trust score
GET /api/trust/network       My lending network (Neo4j graph)
GET /api/trust/network/{id}  Any user's network
```

---

## The Graph Magic ⭐

The unique power of LendGraph is that **Neo4j lets us traverse relationships** between users to detect risk patterns that a traditional SQL database can't.

```cypher
-- Find if borrower has defaulted on anyone in the lender's network
MATCH (borrower:User {id: $borrowerId})-[:BORROWED_BY]-(loan:Loan {status: 'DEFAULTED'})
      -[:LENT_TO]-(networkUser:User)
      -[:LENT_TO|BORROWED_BY*1..2]-(lender:User {id: $lenderId})
RETURN COUNT(loan) as defaultCount
```

**Trust Score Impacts:**
| Event | Score Change |
|-------|-------------|
| Early repayment (7+ days early) | +3.0 |
| On-time repayment | +1.5 |
| Late payment | -5.0 |
| Default | -20.0 |

---

## Deploy to Render

1. Push to GitHub
2. Create new Web Service on [render.com](https://render.com)
3. Connect your GitHub repo
4. Set environment variables in Render dashboard:
   - `NEO4J_URI`
   - `NEO4J_USERNAME`
   - `NEO4J_PASSWORD`
   - `JWT_SECRET`
5. Render auto-deploys on every push to `main`

---

## Graph Model

```
(:User)-[:LENT_TO]->(:Loan)-[:BORROWED_BY]->(:User)
(:Loan)-[:REPAID]->(:Repayment)
(:User)-[:MEMBER_OF]->(:Circle)
(:User)-[:DEFAULTED]->(:TrustEvent)
```

---

Made with ❤️ for HackHazards '26 | Neo4j Track + Render Track
