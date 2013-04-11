
import org.squeryl._
import adapters._
import dsl._
import internals.Utils
import java.sql.ResultSet

object PolicyTypeViewItem extends Enumeration {
  val Fixed = Value("FP")
  val Variable = Value("VP")
}

case class PolicyViewItem1(
    name: String, 
    policyType: String //PolicyTypeViewItem.Value, 
)

case class PolicyViewItem2(
    name: String, 
    policyType: PolicyTypeViewItem.Value
)

trait MyTypes extends PrimitiveTypeMode {
 // ====== Read/Write Enums as string ======

  def enumValueTEF[A >: Enumeration#Value <: Enumeration#Value](ev: Enumeration#Value) = {
    new JdbcMapper[String,A] with TypedExpressionFactory[A,TEnumValue[A]] {
      val enu = Utils.enumerationForValue(ev)
      def extractNativeJdbcValue(rs: ResultSet, i: Int) = rs.getString(i)
      def defaultColumnLength: Int = 4
      def sample: A = ev
      def convertToJdbc(v: A) = v.toString
      def convertFromJdbc(s: String) = {
        enu.values.find(_.toString == s).getOrElse(PrimitiveTypeSupport.DummyEnum.DummyEnumerationValue)
        // JDBC has no concept of null value for primitive types (ex. Int)
        // at this level, we mimic this JDBC flaw (the Option / None based on
        // jdbc.wasNull will get sorted out by optionEnumValueTEF)
      }
    }
  }

  override implicit def enumValueToTE[A <: Enumeration#Value](e: A) =
    enumValueTEF(e).create(e)
}

object MyTypes extends MyTypes
import MyTypes._

object Schema1 extends Schema {
  val items1 = table[PolicyViewItem1]("items")
}

object Schema2 extends Schema {
  val items2 = table[PolicyViewItem2]("items")
}


object Main extends App {
  Class.forName("org.h2.Driver")
  SessionFactory.concreteFactory = Some(()=> Session.create(
    java.sql.DriverManager.getConnection("jdbc:h2:mem:"),
    new H2Adapter))


  import Schema1._
  import Schema2._

  transaction {
    Schema1.create

    val i1 = PolicyViewItem1("name1", "FP")
    items1.insert(i1)

    val i2 = PolicyViewItem2("name2", PolicyTypeViewItem.Fixed)
    items2.insert(i2) // Should save same record as above


    val found1 =  items1.allRows.toList
    println("Found1 "+found1)
    val found2 =  items2.allRows.toList 
    println("Found2 "+found2)
  }
}
