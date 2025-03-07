package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.app.budget.BudgetTable.author
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {

    // Валидация данных для записи бюджета
    private fun validateBudgetRecord(body: BudgetRecord) {
        require(body.year > 0) { "Год должен быть положительным числом." }
        require(body.month in 1..12) { "Месяц должен быть от 1 до 12." }
        require(body.amount in 0..1_000_000) { "Сумма должна быть от 0 до 1,000,000." }

        transaction {
            body.authorId?.let {
                requireNotNull(AuthorEntity.findById(it)) { "Author with ID $it not found" }
            }
        }
    }

    // Добавление записи бюджета
    suspend fun addRecord(body: BudgetRecord): BudgetRecordResponse = withContext(Dispatchers.IO) {
        validateBudgetRecord(body)

        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                body.authorId?.let {
                    this.author = AuthorEntity.findById(it)
                }
            }
            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .select { BudgetTable.year eq param.year }
                .limit(param.limit, param.offset)

            val total = query.count()
            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}