// Copyright 2018 Sourcerer Inc. All Rights Reserved.
// Author: Liubov Yaronskaya (lyaronskaya@sourcerer.io)

package test.tests.hashers

import app.hashers.LogBasedColleagues
import app.model.Author
import app.model.Repo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import test.utils.TestRepo
import java.util.*
import kotlin.test.assertEquals

class LogBasedColleaguesTest : Spek({
    given("repo with a file inside") {
        val testRepoPath = "../log_based_colleagues"
        val testRepo = TestRepo(testRepoPath)
        val serverRepo = Repo(rehash = "test_repo_rehash")
        val fileName = "test1.txt"
        val author1 = Author("First Author", "first.author@gmail.com")
        val author2 = Author("Second Author", "second.author@gmail.com")
        val emails = hashSetOf(author1.email, author2.email)


        testRepo.createFile(fileName, listOf("line1", "line2"))
        testRepo.commit(message = "initial commit",
                author = author1,
                date = Calendar.Builder().setDate(2018, 1, 1).setTimeOfDay
                (0, 0, 0).build().time)

        testRepo.deleteLines(fileName, 1, 1)
        testRepo.commit(message = "delete second line",
                author = author2,
                date = Calendar.Builder().setDate(2018, 1, 1).setTimeOfDay
                (0, 1, 0).build().time)

        testRepo.deleteLines(fileName, 0, 0)
        testRepo.commit(message = "delete first line",
                author = author1,
                date = Calendar.Builder().setDate(2019, 1, 1).setTimeOfDay
                (0, 1, 0).build().time)

        it("creates log file") {
            val log = LogBasedColleagues(serverRepo, testRepoPath, emails,
                    hashSetOf(author1.email)).getLog(fileName, testRepoPath)
            assert(log.contains("+line1") && log.contains("+line2"))
        }

        it("extracts colleagues") {
            val stats = LogBasedColleagues(serverRepo, testRepoPath, emails,
                    hashSetOf(author2.email)).getColleagues()
            assert(stats.containsKey(author1.email))
            assertEquals(1.0, stats[author1.email])
        }

        afterGroup {
            testRepo.destroy()
        }
    }
})
