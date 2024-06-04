package de.vinz.openfls

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.contingents.Contingent
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.entities.*
import java.time.LocalDate
import kotlin.random.Random

class RandomEntityGenerator {

    companion object {
        fun generateRandomAssistancePlan(): AssistancePlan {
            val random = Random.Default

            // Generate random IDs
            val id = random.nextLong()

            // Generate random dates for start and end
            val start = LocalDate.now().plusDays(random.nextInt(1, 30).toLong())
            val end = start.plusDays(random.nextInt(1, 30).toLong())

            // Generate random instances for client, sponsor, and institution (you need to implement these functions)
            val institution = generateEmptyRandomInstitution()
            val categoryTemplate = generateEmptyRandomCategoryTemplate()
            val client = generateRandomClient(institution, categoryTemplate)
            val sponsor = generateRandomSponsor()

            // Create an empty set for goals, hours, services, and employees
            val hours = mutableSetOf<AssistancePlanHour>()
            val services = mutableSetOf<Service>()
            val employees = mutableSetOf<Employee>()

            // Return the generated AssistancePlan instance
            val assistancePlan = AssistancePlan(
                    id = id,
                    start = start,
                    end = end,
                    client = client,
                    sponsor = sponsor,
                    institution = institution,
                    hours = hours,
                    services = services,
                    employees = employees
            )

            assistancePlan.goals = mutableSetOf()

            return assistancePlan
        }

        fun generateRandomGoals(assistancePlan: AssistancePlan, institution: Institution): MutableSet<Goal> {
            val random = Random.Default

            // Define the size of the set
            val setSize = random.nextInt(1, 5)

            val hourTypes = generateHourTypes()
            val goals = mutableSetOf<Goal>()

            for (i in 1..setSize) {
                // Generate random IDs
                val id = random.nextLong()

                // Generate random title and description
                val title = generateRandomString(8)
                val description = generateRandomString(50)

                // Create the Goal instance
                val goal = Goal(
                        id = id,
                        title = title,
                        description = description,
                        institution = institution,
                        assistancePlan = assistancePlan,
                        services = mutableSetOf() // Services will be added separately
                )

                goal.hours = generateRandomGoalHours(goal, getRandomElement(hourTypes))

                // Add the instance to the set
                goals.add(goal)
            }

            return goals
        }

        fun generateRandomGoalHours(goal: Goal, hourType: HourType): MutableSet<GoalHour> {
            val random = Random.Default

            // Define the size of the set
            val setSize = random.nextInt(1, 5)

            val goalHours = mutableSetOf<GoalHour>()

            for (i in 1..setSize) {
                // Generate random IDs
                val id = random.nextLong()

                // Generate random weekly hours
                val weeklyHours = random.nextDouble(1.0, 20.0) // Modify the range as needed

                // Create the GoalHour instance
                val goalHour = GoalHour(
                        id = id,
                        weeklyHours = weeklyHours,
                        hourType = hourType,
                        goal = goal
                )

                // Add the instance to the set
                goalHours.add(goalHour)
            }

            return goalHours
        }

        fun generateHourTypes(): MutableSet<HourType> {
            val random = Random.Default

            // Define the size of the set
            val setSize = random.nextInt(1, 5)

            val hourTypes = mutableSetOf<HourType>()

            for (i in 1..setSize) {
                // Generate random IDs
                val id = random.nextLong()

                // Generate random title and price
                val title = generateRandomString(8)
                val price = random.nextDouble(10.0, 100.0) // Modify the range as needed

                // Create the HourType instance
                val hourType = HourType(
                        id = id,
                        title = title,
                        price = price
                )

                // Add the instance to the set
                hourTypes.add(hourType)
            }

            return hourTypes
        }

        fun generateRandomClient(institution: Institution, categoryTemplate: CategoryTemplate): Client {
            val random = Random.Default

            // Generate random IDs
            val id = random.nextLong()

            // Generate random names and other fields
            val firstName = generateRandomString(10)
            val lastName = generateRandomString(10)
            val phoneNumber = generateRandomPhoneNumber()
            val email = generateRandomEmail()

            // Create an empty set for assistancePlans and services
            val assistancePlans = mutableSetOf<AssistancePlan>()
            val services = mutableSetOf<Service>()

            // Return the generated Client instance
            return Client(
                    id = id,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    email = email,
                    categoryTemplate = categoryTemplate,
                    assistancePlans = assistancePlans,
                    institution = institution,
                    services = services
            )
        }

        fun generateRandomSponsor(): Sponsor {
            val random = Random.Default

            // Generate random IDs
            val id = random.nextLong()

            // Generate random name and other fields
            val name = generateRandomString(8)
            val payOverhang = random.nextBoolean()
            val payExact = random.nextBoolean()

            // Generate random sets of Unprofessional and AssistancePlan instances
            val unprofessionals = mutableSetOf<Unprofessional>()
            val assistancePlans = mutableSetOf<AssistancePlan>()

            // Return the generated Sponsor instance
            return Sponsor(
                    id = id,
                    name = name,
                    payOverhang = payOverhang,
                    payExact = payExact,
                    unprofessionals = unprofessionals,
                    assistancePlans = assistancePlans
            )
        }

        fun generateRandomUnprofessionals(): MutableSet<Unprofessional> {
            val random = Random.Default

            // Define the size of the set
            val setSize = random.nextInt(1, 5)

            val unprofessionals = mutableSetOf<Unprofessional>()

            for (i in 1..setSize) {
                // Generate random IDs
                val employeeId = random.nextLong()
                val sponsorId = random.nextLong()

                // Generate a random Employee and Sponsor instance (you need to implement these functions)
                val employee = generateRandomEmployee()
                val sponsor = generateRandomSponsor()

                // Generate a random end date (nullable)
                val end = if (random.nextBoolean()) LocalDate.now().plusDays(random.nextInt(1, 30).toLong()) else null

                // Create the Unprofessional instance
                val unprofessional = Unprofessional(
                        id = UnprofessionalKey(employeeId, sponsorId),
                        employee = employee,
                        sponsor = sponsor,
                        end = end
                )

                // Add the instance to the set
                unprofessionals.add(unprofessional)
            }

            return unprofessionals
        }

        fun generateRandomEmployee(): Employee {
            val random = Random.Default

            // Generate random IDs
            val id = random.nextLong()

            // Generate random names and other fields
            val firstName = generateRandomString(8)
            val lastName = generateRandomString(8)
            val phoneNumber = generateRandomPhoneNumber()
            val email = generateRandomEmail()

            // Generate random values for other fields
            val inactive = random.nextBoolean()
            val description = generateRandomString(50)

            // Generate a random set of Permission, Unprofessional, and Contingent instances
            val permissions = mutableSetOf<Permission>()
            val unprofessionals = mutableSetOf<Unprofessional>()
            val contingents = mutableSetOf<Contingent>()

            // Create the Employee instance
            return Employee(
                    id = id,
                    firstname = firstName,
                    lastname = lastName,
                    phonenumber = phoneNumber,
                    email = email,
                    inactive = inactive,
                    description = description,
                    access = null, // You might need to create a function to generate EmployeeAccess if needed
                    permissions = permissions,
                    unprofessionals = unprofessionals,
                    contingents = contingents,
                    services = mutableSetOf(), // Services will be added separately
                    assistancePlanFavorites = mutableSetOf() // AssistancePlanFavorites will be added separately
            )
        }

//        fun generateRandomPermissions(): MutableSet<Permission>? {
//            // Implement logic to generate a random set of Permission instances
//        }

//        fun generateRandomContingents(): MutableSet<Contingent>? {
//            // Implement logic to generate a random set of Contingent instances
//        }

        // Implement this function to generate a random set of AssistancePlan instances
//        fun generateRandomAssistancePlans(): MutableSet<AssistancePlan>? {
//            // Implement logic to generate a random set of AssistancePlan instances
//        }

        fun generateEmptyRandomCategoryTemplate(): CategoryTemplate {
            val random = Random.Default

            // Generate random IDs
            val id = random.nextLong()

            // Generate random title and description
            val title = generateRandomString(8)
            val description = generateRandomString(50)

            // Generate a random boolean value for withoutClient
            val withoutClient = random.nextBoolean()

            // Create the CategoryTemplate instance
            val categoryTemplate = CategoryTemplate(
                    id = id,
                    title = title,
                    description = description,
                    withoutClient = withoutClient,
                    categories = mutableSetOf()
            )

            categoryTemplate.categories = mutableSetOf()

            return categoryTemplate
        }

        fun generateRandomCategoryTemplate(): CategoryTemplate {
            val random = Random.Default

            // Generate random IDs
            val id = random.nextLong()

            // Generate random title and description
            val title = generateRandomString(8)
            val description = generateRandomString(50)

            // Generate a random boolean value for withoutClient
            val withoutClient = random.nextBoolean()

            // Create the CategoryTemplate instance
            val categoryTemplate = CategoryTemplate(
                    id = id,
                    title = title,
                    description = description,
                    withoutClient = withoutClient,
                    categories = mutableSetOf()
            )

            categoryTemplate.categories = generateRandomCategories(categoryTemplate)

            return categoryTemplate
        }

        fun generateEmptyRandomInstitution(): Institution {
            val random = Random.Default

            // Generate random IDs
            val id = random.nextLong()

            // Generate random name and other fields
            val name = generateRandomString(8)
            val phoneNumber = generateRandomPhoneNumber()
            val email = generateRandomEmail()

            // Generate a random set of Permission, Contingent, AssistancePlan, and Goal instances
            val permissions = mutableSetOf<Permission>()
            val contingents = mutableSetOf<Contingent>()
            val assistancePlans = mutableSetOf<AssistancePlan>()
            val goals = mutableSetOf<Goal>()

            // Create the Institution instance
            return Institution(
                    id = id,
                    name = name,
                    phonenumber = phoneNumber,
                    email = email,
                    permissions = permissions,
                    contingents = contingents,
                    assistancePlans = assistancePlans,
                    goals = goals,
                    services = mutableSetOf() // Services will be added separately
            )
        }

        fun generateRandomCategories(categoryTemplate: CategoryTemplate): MutableSet<Category> {
            val random = Random.Default

            // Define the size of the set
            val setSize = random.nextInt(1, 5)

            val categories = mutableSetOf<Category>()

            for (i in 1..setSize) {
                // Generate random IDs
                val id = random.nextLong()

                // Generate random title, shortcut, and description
                val title = generateRandomString(8)
                val shortcut = generateRandomString(4)
                val description = generateRandomString(50)

                // Generate a random boolean value for faceToFace
                val faceToFace = random.nextBoolean()

                // Create the Category instance
                val category = Category(
                        id = id,
                        title = title,
                        shortcut = shortcut,
                        description = description,
                        faceToFace = faceToFace,
                        categoryTemplate = categoryTemplate,
                        services = mutableSetOf() // Services will be added separately
                )

                // Add the instance to the set
                categories.add(category)
            }

            return categories
        }

        private fun generateRandomString(length: Int): String {
            val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return (1..length)
                    .map { chars.random() }
                    .joinToString("")
        }

        private fun generateRandomPhoneNumber(): String {
            return "+1${Random.Default.nextInt(100, 999)}-${Random.Default.nextInt(100, 999)}-${Random.Default.nextInt(1000, 9999)}"
        }

        private fun generateRandomEmail(): String {
            return "${generateRandomString(8)}@example.com"
        }

        private fun <T> getRandomElement(set: MutableSet<T>): T {
            val list = set.toList()
            val randomIndex = Random.nextInt(list.size)
            return list[randomIndex]
        }
    }
}