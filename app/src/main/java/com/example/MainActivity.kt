package com.example

import android.content.Context
import android.print.PrintManager
import android.print.PrintAttributes
import android.webkit.WebView
import android.webkit.WebViewClient
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

enum class AppLanguage {
    EN, FI
}

val LocalLanguage = staticCompositionLocalOf { AppLanguage.EN }

@Composable
fun trans(en: String, fi: String): String {
    return if (LocalLanguage.current == AppLanguage.EN) en else fi
}

fun transWithLanguage(lang: AppLanguage, en: String, fi: String): String {
    return if (lang == AppLanguage.EN) en else fi
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      var isDarkTheme by remember { mutableStateOf(false) }
      val systemTheme = isSystemInDarkTheme()
      
      // Initialize with system theme on first composition
      LaunchedEffect(Unit) {
          isDarkTheme = systemTheme
      }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        PortfolioSleekApp(
            isDarkTheme = isDarkTheme,
            onThemeToggle = { isDarkTheme = !isDarkTheme },
            modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

@Composable
fun PortfolioSleekApp(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf("Home") }
  var currentLanguage by remember { mutableStateOf(AppLanguage.EN) }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val showScrollToTop by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 250
    }
  }

  LaunchedEffect(selectedTab) {
    if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
      listState.animateScrollToItem(0)
    }
  }

  CompositionLocalProvider(LocalLanguage provides currentLanguage) {
    Scaffold(
      modifier = modifier,
      containerColor = MaterialTheme.colorScheme.background,
      bottomBar = { 
          BottomNavigationBar(
              selectedTab = selectedTab,
              onTabSelected = { selectedTab = it }
          ) 
      },
      floatingActionButton = {
        AnimatedVisibility(
          visible = showScrollToTop,
          enter = fadeIn(animationSpec = tween(200)) + scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
          exit = fadeOut(animationSpec = tween(150)) + scaleOut(animationSpec = tween(150))
        ) {
          FloatingActionButton(
            onClick = {
              coroutineScope.launch {
                listState.animateScrollToItem(0)
              }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
            shape = CircleShape,
            modifier = Modifier.testTag("scroll_to_top_button")
          ) {
            Icon(
              imageVector = Icons.Default.KeyboardArrowUp,
              contentDescription = trans("Scroll to top", "Siirry ylös")
            )
          }
        }
      }
    ) { innerPadding ->
      LazyColumn(
        state = listState,
        modifier = Modifier.padding(innerPadding).fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
      ) {
        item { 
          HeaderSection(
              isDarkTheme = isDarkTheme, 
              onThemeToggle = onThemeToggle, 
              currentLanguage = currentLanguage,
              onLanguageChange = { currentLanguage = it }
          ) 
        }
        
        item {
        AnimatedContent(
          targetState = selectedTab,
          transitionSpec = {
            (fadeIn(animationSpec = tween(350)) + slideInHorizontally(animationSpec = tween(350)) { width -> width / 4 })
              .togetherWith(fadeOut(animationSpec = tween(250)) + slideOutHorizontally(animationSpec = tween(250)) { width -> -width / 4 })
          },
          label = "SectionTransition"
        ) { targetTab ->
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
          ) {
            when (targetTab) {
              "Home" -> {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    HeroSection(onViewWorkClick = { selectedTab = "Projects" })
                }
                ContentSection()
              }
              "Projects" -> {
                ProjectsSection()
              }
              "About" -> {
                AboutSection()
              }
              "Contact" -> {
                ContactSection()
              }
            }
          }
        }
      }
    }
  }
}
}

@Composable
fun HeaderSection(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "JK",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontWeight = FontWeight.Bold
      )
    }
    
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // PDF Print/Download Button
      FilledTonalButton(
        onClick = { triggerPdfPrint(context) },
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = Modifier.height(38.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Print,
          contentDescription = "Print or Save CV to PDF",
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = trans("PDF CV", "Lataa PDF"),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold
        )
      }

      // Language Switcher Toggle Row
      Row(
        modifier = Modifier
          .height(38.dp)
          .clip(RoundedCornerShape(19.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        listOf(AppLanguage.EN, AppLanguage.FI).forEach { lang ->
          val isSelected = lang == currentLanguage
          Box(
            modifier = Modifier
              .fillMaxHeight()
              .clip(RoundedCornerShape(17.dp))
              .background(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
              )
              .clickable { onLanguageChange(lang) }
              .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (lang == AppLanguage.EN) "EN" else "FI",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Theme toggle
      IconButton(
        onClick = onThemeToggle,
        modifier = Modifier
          .size(38.dp)
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
      ) {
        Icon(
          imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
          contentDescription = "Toggle Theme",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

fun triggerPdfPrint(context: Context) {
    try {
        val webView = WebView(context)
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Jaakko Kallio - CV</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        color: #1A202C;
                        line-height: 1.5;
                        margin: 0;
                        padding: 30px;
                        background-color: #ffffff;
                    }
                    .header {
                        border-bottom: 3px solid #5D3FCF;
                        padding-bottom: 16px;
                        margin-bottom: 24px;
                    }
                    .name {
                        font-size: 28px;
                        font-weight: 800;
                        color: #111111;
                        margin: 0;
                        letter-spacing: -0.5px;
                    }
                    .title {
                        font-size: 18px;
                        color: #5D3FCF;
                        margin: 4px 0 12px 0;
                        font-weight: 600;
                    }
                    .contact-info {
                        font-size: 13px;
                        color: #4A5568;
                        display: flex;
                        gap: 20px;
                        flex-wrap: wrap;
                        margin-bottom: 12px;
                    }
                    .contact-item {
                        display: flex;
                        align-items: center;
                    }
                    .bio {
                        font-size: 13.5px;
                        color: #4A5568;
                        margin-top: 12px;
                        line-height: 1.6;
                    }
                    .section-title {
                        font-size: 16px;
                        font-weight: 700;
                        color: #111111;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                        margin-top: 28px;
                        margin-bottom: 14px;
                        border-bottom: 1px solid #E2E8F0;
                        padding-bottom: 4px;
                        page-break-after: avoid;
                    }
                    .item {
                        margin-bottom: 18px;
                        page-break-inside: avoid;
                    }
                    .item-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: baseline;
                        margin-bottom: 4px;
                    }
                    .item-title {
                        font-size: 15px;
                        font-weight: 700;
                        color: #1A202C;
                    }
                    .item-subtitle {
                        font-size: 13px;
                        color: #5D3FCF;
                        font-weight: 600;
                        text-align: right;
                    }
                    .bullet-list {
                        margin: 4px 0 0 0;
                        padding-left: 18px;
                    }
                    .item-bullet {
                        font-size: 13px;
                        color: #4A5568;
                        margin-bottom: 4px;
                    }
                    .skills-grid {
                        display: grid;
                        grid-template-columns: 1fr 1fr 1fr;
                        gap: 16px;
                        margin-top: 8px;
                        page-break-inside: avoid;
                    }
                    .skill-group {
                        background-color: #F8FAFC;
                        padding: 12px;
                        border-radius: 8px;
                        border: 1px solid #E2E8F0;
                    }
                    .skill-group-title {
                        font-size: 13px;
                        font-weight: 750;
                        color: #5D3FCF;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        margin-bottom: 6px;
                    }
                    .skill-list {
                        font-size: 12px;
                        color: #4A5568;
                        line-height: 1.5;
                    }
                    @media print {
                        body {
                            padding: 15px;
                        }
                        .skill-group {
                            background-color: #ffffff;
                            border: 1px solid #CBD5E1;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1 class="name">Jaakko Kallio</h1>
                    <div class="title">Full Stack Engineer & UI Craftsman</div>
                    <div class="contact-info">
                        <div class="contact-item"><strong>Location:</strong>&nbsp;Helsinki, FI</div>
                        <div class="contact-item"><strong>Email:</strong>&nbsp;jaakko.kkallio@gmail.com</div>
                        <div class="contact-item"><strong>Portfolio:</strong>&nbsp;jaakkokallio.dev</div>
                    </div>
                    <div class="bio">
                        Passionate about building clean, high-performance declarative systems. Expert at structuring robust Jetpack Compose frontends with lightweight, modern backends.
                    </div>
                </div>

                <div class="section-title">Professional Experience</div>
                
                <div class="item">
                    <div class="item-header">
                        <span class="item-title">Lead Full Stack Developer</span>
                        <span class="item-subtitle">CloudSphere Inc. &bull; 2022 - Present</span>
                    </div>
                    <ul class="bullet-list">
                        <li class="item-bullet">Spearheaded development of a high-performance analytics platform with React & Go.</li>
                        <li class="item-bullet">Architected WebSockets streaming pipelines, reducing render-delay overhead by 40%.</li>
                        <li class="item-bullet">Led an agile team of 4 remote engineer peers in sprint planning and design handoffs.</li>
                    </ul>
                </div>

                <div class="item">
                    <div class="item-header">
                        <span class="item-title">Senior Android Craftsman</span>
                        <span class="item-subtitle">CryptoDash Mobile &bull; 2020 - 2022</span>
                    </div>
                    <ul class="bullet-list">
                        <li class="item-bullet">Designed fluid Jetpack Compose portfolio boards capturing fast cryptocurrency states.</li>
                        <li class="item-bullet">Engineered Room DB local cache synchronization flow, keeping offline capability pristine.</li>
                        <li class="item-bullet">Integrated secure OAuth2 identity services and optimized overall battery drain efficiency.</li>
                    </ul>
                </div>

                <div class="item">
                    <div class="item-header">
                        <span class="item-title">Software Systems Engineer</span>
                        <span class="item-subtitle">Nordic Devs Agency &bull; 2018 - 2020</span>
                    </div>
                    <ul class="bullet-list">
                        <li class="item-bullet">Implemented bespoke cloud tools and backend REST APIs utilizing Node.js & Docker.</li>
                        <li class="item-bullet">Crafted custom dynamic charting modules with outstanding UI micro-interactions.</li>
                    </ul>
                </div>

                <div class="section-title">Education</div>
                
                <div class="item">
                    <div class="item-header">
                        <span class="item-title">Master of Science in Computer Science</span>
                        <span class="item-subtitle">Aalto University &bull; 2016 - 2018</span>
                    </div>
                    <ul class="bullet-list">
                        <li class="item-bullet">Major in Declarative Systems and Software Architectures. Developed a reactive state flow prototype for high-density logistics grids. Graduated with Honors (GPA: 4.8/5.0).</li>
                    </ul>
                </div>

                <div class="item">
                    <div class="item-header">
                        <span class="item-title">Bachelor of Computer Science</span>
                        <span class="item-subtitle">University of Helsinki &bull; 2013 - 2016</span>
                    </div>
                    <ul class="bullet-list">
                        <li class="item-bullet">Specialized study in algorithmic efficiency, operational databases, and discrete systems. Minored in Industrial Design & Interface Usability.</li>
                    </ul>
                </div>

                <div class="section-title">Technical Expertise</div>
                <div class="skills-grid">
                    <div class="skill-group">
                        <div class="skill-group-title">Languages & DB</div>
                        <div class="skill-list">Kotlin, Java, Go (Golang), TypeScript, JS, SQL, HTML/CSS</div>
                    </div>
                    <div class="skill-group">
                        <div class="skill-group-title">Frontend & UI</div>
                        <div class="skill-list">Jetpack Compose, React (Next.js), Coroutines & Flow, Adaptive Layouts</div>
                    </div>
                    <div class="skill-group">
                        <div class="skill-group-title">DevOps & Cloud</div>
                        <div class="skill-list">Git/GitHub Actions, Docker, AWS Suite, Firebase Platform</div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                try {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val jobName = "Jaakko_Kallio_CV"
                    val printAdapter = view?.createPrintDocumentAdapter(jobName)
                    if (printAdapter != null) {
                        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Error printing CV: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Printing unavailable: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

@Composable
fun GreetingSection() {
  Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
    Text(
      text = trans("Hello, I'm ", "Hei, olen "),
      style = MaterialTheme.typography.displayMedium,
      color = MaterialTheme.colorScheme.onBackground
    )
    Text(
      text = "Jaakko",
      style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = trans("Full Stack Developer & UI Designer", "Full Stack -kehittäjä & käyttöliittymäsuunnittelija"),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentSection() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Featured Project Card
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
      modifier = Modifier.fillMaxWidth().clickable { }
    ) {
      Column(modifier = Modifier.padding(24.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Outlined.RocketLaunch,
            contentDescription = "Featured Project",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(32.dp)
          )
          Surface(
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = CircleShape
          ) {
            Text(
              text = trans("FEATURED", "PINNALLA"),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
              fontWeight = FontWeight.Bold
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Project Zenith",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = trans(
            "A high-performance cloud monitoring dashboard built with React and Go.",
            "Reactilla ja Go-kielellä rakennettu tehokas pilviseurannan hallintapaneeli."
          ),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          SkillChip("TYPESCRIPT")
          SkillChip("AWS")
        }
      }
    }

    // Grid row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      StatsCard(
        icon = Icons.Outlined.Terminal,
        title = trans("Tech Stack", "Teknologiat"),
        subtitle = trans("12+ Technologies", "12+ Teknologiaa"),
        modifier = Modifier.weight(1f)
      )
      StatsCard(
        icon = Icons.Outlined.HistoryEdu,
        title = trans("Experience", "Työkokemus"),
        subtitle = trans("5 Years Active", "5 vuotta aktiivisena"),
        modifier = Modifier.weight(1f)
      )
    }

    // Contact Card
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth().clickable { }
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Mail,
              contentDescription = "Mail",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
          Column {
            Text(
              text = trans("Get in touch", "Ota yhteyttä"),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = trans("Available for hire", "Valmiina uusiin haasteisiin"),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        Icon(
          imageVector = Icons.Outlined.ChevronRight,
          contentDescription = "Go",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

data class ProjectItem(
    val title: String,
    val description: String,
    val technologies: List<String>,
    val linkText: String,
    val imageIcon: ImageVector,
    val projectImages: List<String> = emptyList(),
    val category: String,
    val architectureHighlights: List<String>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectsSection() {
    var selectedCategory by remember { mutableStateOf("All") }
    val currentLang = LocalLanguage.current
    val projects = remember(currentLang) {
        listOf(
            ProjectItem(
                title = "Project Zenith",
                description = transWithLanguage(
                    currentLang,
                    "A high-performance cloud monitoring dashboard built with React and Go. Features real-time telemetry metrics streaming, customizable widget compositions, and fine-grained access control boundaries.",
                    "Reactilla ja Go-kielellä rakennettu tehokas pilviseurannan hallintapaneeli. Sisältää reaaliaikaisen telemetriatietojen striimauksen, muokattavat widget-koosteet ja hienojakoisen käyttöoikeuksien hallinnan."
                ),
                technologies = listOf("React", "Go & gRPC", "TypeScript", "AWS Cloud", "WebSockets", "Docker"),
                linkText = transWithLanguage(currentLang, "Live Demo • zenith.dev", "Live-demo • zenith.dev"),
                imageIcon = Icons.Outlined.Cloud,
                projectImages = listOf(
                    "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1547082299-de196ea013d6?auto=format&fit=crop&w=600&q=80"
                ),
                category = "Full Stack",
                architectureHighlights = listOf(
                    transWithLanguage(currentLang, "Configured distributed WebSockets gateway supporting up to 50k active subscription loops.", "Konfiguroi hajautetun WebSockets-yhteyskäytävän, joka tukee jopa 50 tuhatta aktiivista tilaussilmukkaa."),
                    transWithLanguage(currentLang, "Designed zero-alloc metric serialization structures in Go, reducing garbage collection latency by 35%.", "Suunnitteli zero-alloc-metriikan sarjoitusrakenteet Go-kielellä, vähentäen roskienkeruun viivettä 35 %."),
                    transWithLanguage(currentLang, "Deployed containerized ECS tasks with dynamic Auto-Scaling schedules matching load profiles.", "Otettu käyttöön kontitetut ECS-tehtävät dynaamisilla automaattisen skaalauksen aikatauluilla kuormitusprofiilien mukaan.")
                )
            ),
            ProjectItem(
                title = "CryptoDash Mobile",
                description = transWithLanguage(
                    currentLang,
                    "A responsive cryptocurrency portfolio and real-time ledger tracking application. Integrates full offline capabilities and customizable visual candlestick analytics widgets.",
                    "Responsiivinen kryptovaluuttasalkku- ja reaaliaikainen tilikirjasovellus. Sisältää täydelliset offline-toiminnot ja muokattavat dynaamiset kynttiläkaavioanalytiikan widgetit."
                ),
                technologies = listOf("Kotlin", "Jetpack Compose", "Coroutines & Flow", "Room DB", "Retrofit", "M3 Design"),
                linkText = transWithLanguage(currentLang, "Source Code • GitHub/CryptoDash", "Lähdekoodi • GitHub/CryptoDash"),
                imageIcon = Icons.AutoMirrored.Outlined.TrendingUp,
                projectImages = listOf(
                    "https://images.unsplash.com/photo-1621416894569-0f39ed31d247?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1518546305927-5a555bb7020d?auto=format&fit=crop&w=600&q=80"
                ),
                category = "Mobile",
                architectureHighlights = listOf(
                    transWithLanguage(currentLang, "Customized state synchronization pipeline via Kotlin Flows to merge offline-first local data cleanly with retrofitted REST APIs.", "Räätälöity tilan synkronointiputki Kotlin Flow'lla yhdistämään offline-paikallistiedot saumattomasti palvelimen REST-rajapintoihin."),
                    transWithLanguage(currentLang, "Implemented responsive canvas charting engines supporting multi-finger drag, tap to inspect, and local pinch-to-zoom gestures.", "Toteutti dynaamisen piirtoalustan graafisille kuvaajille, tukien monisormieleitä, kosketustarkastelua sekä nipistyszoomausta.")
                )
            ),
            ProjectItem(
                title = "AuraSound Engine",
                description = transWithLanguage(
                    currentLang,
                    "Low-latency dynamic soundscape synthesize manager designed for productivity and relaxation tracking. Houses foreground audio services, custom equalizer sliders, and profile states.",
                    "Matalaviiveinen dynaamisen äänimaiseman synteesityökalu tuottavuuden ja rentoutumisen tueksi. Sisältää taustaäänipalvelut, mukautetut taajuuskorjaimet ja käyttäjäprofiilit."
                ),
                technologies = listOf("Kotlin", "Jetpack Compose", "Jetpack Media3", "Coroutines", "Room Database"),
                linkText = transWithLanguage(currentLang, "Source Code • GitHub/AuraSound", "Lähdekoodi • GitHub/AuraSound"),
                imageIcon = Icons.Outlined.AudioFile,
                projectImages = listOf(
                    "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1511379938547-c1f69419868d?auto=format&fit=crop&w=600&q=80"
                ),
                category = "Mobile",
                architectureHighlights = listOf(
                    transWithLanguage(currentLang, "Integrated Android Foreground Service with MediaSession hooks for uninterrupted background playing states.", "Integroi Androidin Foreground Service -taustapalvelun MediaSession-kytkennöillä keskeytymättömään taustatoistoon."),
                    transWithLanguage(currentLang, "Optimized procedural sound generation parameters, eliminating UI thread audio crackles and frame drops.", "Optimoi proseduraalisen äänisynteesin parametreja, poistaen käyttöliittymäsäikeen audiorätinät ja kuvataajuuden pudotukset.")
                )
            ),
            ProjectItem(
                title = "EventHorizon Broker",
                description = transWithLanguage(
                    currentLang,
                    "A custom distributed transaction message broker offering exceptional packet throughput, low-latency consensus, and high horizontal scalability with detailed health metrics.",
                    "Hajautettu siirtotapahtumaviestinvälittäjä erinomaisella pakettien läpisyöttökyvyllä, matalaviiveisellä konsensuksella ja korkealla vaakasuoralla skaalautuvuudella."
                ),
                technologies = listOf("Go (Golang)", "gRPC & Protobuf", "Docker", "Kubernetes", "Prometheus"),
                linkText = transWithLanguage(currentLang, "Technical Readme • eventhorizon.org", "Tekninen Readme • eventhorizon.org"),
                imageIcon = Icons.Outlined.Hub,
                projectImages = listOf(
                    "https://images.unsplash.com/photo-1504868584819-f8e8b4b6d7e3?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&w=600&q=80"
                ),
                category = "Cloud & Sys",
                architectureHighlights = listOf(
                    transWithLanguage(currentLang, "Architected write-ahead file-system logging algorithms to guarantee message preservation under cluster crashes.", "Suunnitteli write-ahead-tiedostojärjestelmän kirjausalgoritmeja varmistamaan viestien säilymisen klusterikatkosten aikana."),
                    transWithLanguage(currentLang, "Implemented custom Raft consensus mechanism yielding failover times under 200 milliseconds.", "Toteutti mukautetun Raft-konsensusmekanismin, joka mahdollistaa vikasietoisuuden alle 200 millisekunnissa.")
                )
            )
        )
    }

    val categories = remember { listOf("All", "Mobile", "Full Stack", "Cloud & Sys") }
    val filteredProjects = remember(selectedCategory, projects) {
        if (selectedCategory == "All") projects else projects.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = trans("Projects", "Projektit"),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = trans(
                    "A curated collection of production systems, user-centric mobile applications, and backend frameworks.",
                    "Kuratoitu kokoelma tuotantojärjestelmiä, käyttäjälähtöisiä mobiilisovelluksia sekä taustajärjestelmäkehyksiä."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Horizontal Category Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                val displayText = when (category) {
                    "All" -> trans("All", "Kaikki")
                    "Mobile" -> trans("Mobile", "Mobiili")
                    "Full Stack" -> trans("Full Stack", "Full Stack")
                    "Cloud & Sys" -> trans("Cloud & Sys", "Pilvi & Järjestelmät")
                    else -> category
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { 
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }

        // Animated Switch of the list
        AnimatedContent(
            targetState = filteredProjects,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "ProjectsListChange"
        ) { currentProjects ->
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                currentProjects.forEach { project ->
                    DetailedProjectCard(project = project)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailedProjectCard(
    project: ProjectItem
) {
    var isExpanded by remember { mutableStateOf(false) }

    ScrollIntersectionObserverWrapper {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
            // Header Info Row with Asymmetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = project.imageIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Category Badge
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            val dispCat = when (project.category) {
                                "Mobile" -> trans("Mobile", "Mobiili")
                                "Full Stack" -> trans("Full Stack", "Full Stack")
                                "Cloud & Sys" -> trans("Cloud & Sys", "Pilvi & Järjestelmät")
                                else -> project.category
                            }
                            Text(
                                text = dispCat,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tech Badges flow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                project.technologies.forEach { tech ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tech,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Expandable details (Architectural Highlights & Challenges)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = trans("Engineering Highlights", "Tuotekehityksen kohokohdat"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    project.architectureHighlights.forEach { bullet ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bullet,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Project Screenshots LazyRow Carousel
            if (project.projectImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(project.projectImages) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Project snapshot preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(260.dp, 150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer row with Expand toggle & Link Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand / Collapse details
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isExpanded) trans("Hide Highlights", "Piilota lisätiedot") else trans("Show Highlights", "Näytä lisätiedot"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Launch Link Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { /* action */ }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = "Explore link",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = project.linkText.split(" • ").first(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun ScrollIntersectionObserverWrapper(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = remember(density, configuration) {
        with(density) { configuration.screenHeightDp.dp.toPx() }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "scroll_fade"
    )
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scroll_slide"
    )

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                if (!isVisible) {
                    val positionInWindow = coordinates.positionInWindow()
                    // Trigger fade-in when the component reaches the lower 92% of screen height
                    if (positionInWindow.y < screenHeightPx * 0.92f) {
                        isVisible = true
                    }
                }
            }
            .graphicsLayer {
                alpha = animatedAlpha
                translationY = animatedOffsetY.toPx()
            }
    ) {
        content()
    }
}

@Composable
fun HeroSection(onViewWorkClick: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.jaakko_avatar),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Jaakko Kallio",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = trans("Full Stack Engineer & UI Craftsman", "Full Stack -kehittäjä & käyttöliittymäsuunnittelija"),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = trans(
                    "Passionate about building clean, high-performance declarative systems. Expert at structuring robust Jetpack Compose frontends with lightweight, modern backends.",
                    "Intohimona puhtaat ja erittäin suorituskykyiset deklaratiiviset ohjelmistot. Kokemusta vankkojen Jetpack Compose -käyttöliittymien ja keveiden backendien rakentamisesta."
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text("Helsinki, FI") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Room,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(modifier = Modifier.width(12.dp))
                AssistChip(
                    onClick = { },
                    label = { Text(trans("Active for Hire", "Vapaa haasteisiin")) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onViewWorkClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Text(
                    text = trans("View My Work", "Katso töitäni"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AboutSection() {
    var activeSubTab by remember { mutableStateOf("Experience") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // CV Page Segmented Inner Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Experience", "Education", "Skills").forEach { tab ->
                val isSelected = activeSubTab == tab
                val displayText = when (tab) {
                    "Experience" -> trans("Experience", "Työkokemus")
                    "Education" -> trans("Education", "Koulutus")
                    "Skills" -> trans("Skills", "Osaaminen")
                    else -> tab
                }
                Button(
                    onClick = { activeSubTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Tab Content Section with Animations
        AnimatedContent(
            targetState = activeSubTab,
            transitionSpec = {
                (fadeIn(animationSpec = spring()) + slideInVertically { it / 3 })
                    .togetherWith(fadeOut(animationSpec = spring()))
            },
            label = "CvTabTransition"
        ) { targetTab ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (targetTab) {
                    "Experience" -> {
                        ExperienceTimeline()
                    }
                    "Education" -> {
                        EducationTimeline()
                    }
                    "Skills" -> {
                        DetailedSkillsView()
                    }
                }
            }
        }
    }
}

@Composable
fun ExperienceTimeline() {
    val jobs = listOf(
        Triple(
            trans("Lead Full Stack Developer", "Johtava Full Stack -kehittäjä"),
            trans("CloudSphere Inc.  •  2022 - Present", "CloudSphere Inc.  •  2022 - Nykyhetki"),
            listOf(
                trans("Spearheaded development of a high-performance analytics platform with React & Go.", "Johti Reactilla & Golla toteutetun korkean suorituskyvyn analytiikka-alustan kehitystä."),
                trans("Architected WebSockets streaming pipelines, reducing render-delay overhead by 40%.", "Suunnitteli WebSockets-striimausputket, vähentäen renderöintiviiveitä 40 %."),
                trans("Led an agile team of 4 remote engineer peers in sprint planning and design handoffs.", "Johti nelihenkisen etäkehitysryhmän sprinttisuunnittelua ja suunnittelutyön luovutusta.")
            )
        ),
        Triple(
            trans("Senior Android Craftsman", "Vanhempi Android-kehittäjä"),
            trans("CryptoDash Mobile  •  2020 - 2022", "CryptoDash Mobile  •  2020 - 2022"),
            listOf(
                trans("Designed fluid Jetpack Compose portfolio boards capturing fast cryptocurrency states.", "Suunnitteli sulavia Jetpack Compose -näkymiä nopeasti muuttuvien kryptovaluuttatietojen esittämiseen."),
                trans("Engineered Room DB local cache synchronization flow, keeping offline capability pristine.", "Kehitti Room-tietokannan paikallisen välimuistin synkronointitoteutuksen offline-toiminnallisuuden varmistamiseksi."),
                trans("Integrated secure OAuth2 identity services and optimized overall battery drain efficiency.", "Integroi suojatut OAuth2-tunnistautumispalvelut ja optimoi akunkulutusta.")
            )
        ),
        Triple(
            trans("Software Systems Engineer", "Ohjelmistojärjestelmäinsinööri"),
            trans("Nordic Devs Agency  •  2018 - 2020", "Nordic Devs Agency  •  2018 - 2020"),
            listOf(
                trans("Implemented bespoke cloud tools and backend REST APIs utilizing Node.js & Docker.", "Toteutti räätälöityjä pilvityökaluja sekä REST-rajapintoja Node.js- ja Docker-teknologioilla."),
                trans("Crafted custom dynamic charting modules with outstanding UI micro-interactions.", "Rakensi kustomoituja dynaamisia kuvaajamoduuleita erinomaisilla käyttöliittymän mikrointeraktioilla.")
            )
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        jobs.forEach { (role, details, bullets) ->
            ScrollIntersectionObserverWrapper {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = role,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Outlined.HistoryEdu,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = details,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        bullets.forEach { bullet ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = bullet,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EducationTimeline() {
    val degrees = listOf(
        Triple(
            trans("Master of Science in Computer Science", "Diplomi-insinööri, Tietotekniikka"),
            trans("Aalto University  •  2016 - 2018", "Aalto-yliopisto  •  2016 - 2018"),
            trans(
                "Major in Declarative Systems and Software Architectures. Developed a reactive state flow prototype for high-density logistics grids. Graduated with Honors (GPA: 4.8/5.0).",
                "Pääaineena deklaratiiviset järjestelmät ja ohjelmistoarkkitehtuurit. Kehitti reaktiivisen tilanvirtausprototyypin tiheille logistiikkaverkoille. Valmistui erinomaisin arvosanoin (keskiarvo: 4.8/5.0)."
            )
        ),
        Triple(
            trans("Bachelor of Computer Science", "Luonnontieteiden kandidaatti, Tietojenkäsittelytiede"),
            trans("University of Helsinki  •  2013 - 2016", "Helsingin yliopisto  •  2013 - 2016"),
            trans(
                "Specialized study in algorithmic efficiency, operational databases, and discrete systems. Minored in Industrial Design & Interface Usability.",
                "Erikoistumisopintoina algoritmien tehokkuus, käyttöjärjestelmät/tietokannat ja diskreetit järjestelmät. Sivuaineina teollinen muotoilu ja käyttöliittymien käytettävyys."
            )
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        degrees.forEach { (degree, institution, desc) ->
            ScrollIntersectionObserverWrapper {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = degree,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Outlined.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = institution,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveRadarChart() {
    var selectedCategory by remember { mutableStateOf("Full") }
    val currentLang = LocalLanguage.current
    
    val skills = remember(selectedCategory, currentLang) {
        when (selectedCategory) {
            "Programming" -> listOf(
                Pair(transWithLanguage(currentLang, "Kotlin/Java", "Kotlin/Java"), 0.95f),
                Pair(transWithLanguage(currentLang, "TS/JS", "TS/JS"), 0.85f),
                Pair(transWithLanguage(currentLang, "Go Language", "Go-kieli"), 0.80f),
                Pair(transWithLanguage(currentLang, "SQL/HTML", "SQL/HTML"), 0.75f)
            )
            "Frontend" -> listOf(
                Pair(transWithLanguage(currentLang, "Compose", "Compose"), 0.98f),
                Pair(transWithLanguage(currentLang, "React JS", "React JS"), 0.85f),
                Pair(transWithLanguage(currentLang, "Coroutines", "Käynnistys"), 0.92f),
                Pair(transWithLanguage(currentLang, "Responsive", "Responsiivisuus"), 0.90f)
            )
            "DevOps" -> listOf(
                Pair(transWithLanguage(currentLang, "Git/CI", "Git/CI"), 0.90f),
                Pair(transWithLanguage(currentLang, "Docker", "Docker"), 0.80f),
                Pair(transWithLanguage(currentLang, "AWS Cloud", "AWS Pilvi"), 0.70f),
                Pair(transWithLanguage(currentLang, "Firebase", "Firebase"), 0.88f)
            )
            else -> listOf(
                Pair(transWithLanguage(currentLang, "Kotlin/Java", "Kotlin/Java"), 0.95f),
                Pair(transWithLanguage(currentLang, "Compose", "Compose"), 0.98f),
                Pair(transWithLanguage(currentLang, "React JS", "React JS"), 0.85f),
                Pair(transWithLanguage(currentLang, "Go Engine", "Go-kieli"), 0.80f),
                Pair(transWithLanguage(currentLang, "Git/CI", "Git/CI"), 0.90f),
                Pair(transWithLanguage(currentLang, "Firebase", "Firebase"), 0.88f)
            )
        }
    }
    
    val animationTrigger = remember(selectedCategory) { Animatable(0f) }
    LaunchedEffect(selectedCategory) {
        animationTrigger.snapTo(0f)
        animationTrigger.animateTo(
            targetValue = 1f,
            animationSpec = tween(700, easing = FastOutSlowInEasing)
        )
    }
    
    val textMeasurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val accentColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = transWithLanguage(currentLang, "Skill Proficiency Matrix", "Osaamisen tutkakuvio"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = transWithLanguage(
                    currentLang,
                    "Visual representation of technical depth and system design capabilities.",
                    "Graafinen visualisointi teknisestä osaamisesta ja arkkitehtuureista."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp).align(Alignment.Start)
            )
            
            // Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    Triple("Full", transWithLanguage(currentLang, "All", "Kaikki"), Icons.Outlined.Star),
                    Triple("Programming", transWithLanguage(currentLang, "Dev", "Luonti"), Icons.Outlined.Code),
                    Triple("Frontend", transWithLanguage(currentLang, "UI", "Käyttö"), Icons.Outlined.Draw),
                    Triple("DevOps", transWithLanguage(currentLang, "Ops", "Ajo"), Icons.Outlined.Settings)
                )
                
                tabs.forEach { (tabKey, tabLabel, icon) ->
                    val isSelected = selectedCategory == tabKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = tabKey },
                        label = { 
                            Text(
                                text = tabLabel, 
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Recharts-style Radar Chart inside Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val cx = width / 2f
                    val cy = height / 2f
                    val maxRadius = minOf(width, height) / 2f * 0.65f
                    
                    val n = skills.size
                    val gridLevels = listOf(0.25f, 0.50f, 0.75f, 1.00f)
                    
                    // Draw concentric grid lines
                    gridLevels.forEach { levelFrac ->
                        val path = Path()
                        for (i in 0 until n) {
                            val angle = -PI.toFloat() / 2f + i * (2f * PI.toFloat() / n)
                            val rx = cx + maxRadius * levelFrac * cos(angle)
                            val ry = cy + maxRadius * levelFrac * sin(angle)
                            if (i == 0) {
                                path.moveTo(rx, ry)
                            } else {
                                path.lineTo(rx, ry)
                            }
                        }
                        path.close()
                        drawPath(
                            path = path,
                            color = gridColor,
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        )
                    }
                    
                    // Draw grid spoke lines
                    for (i in 0 until n) {
                        val angle = -PI.toFloat() / 2f + i * (2f * PI.toFloat() / n)
                        val rx = cx + maxRadius * cos(angle)
                        val ry = cy + maxRadius * sin(angle)
                        drawLine(
                            color = gridColor.copy(alpha = 0.5f),
                            start = Offset(cx, cy),
                            end = Offset(rx, ry),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    // Draw animated radar data filled polygon
                    val dataPath = Path()
                    val dataPoints = mutableListOf<Offset>()
                    for (i in 0 until n) {
                        val (_, proficiency) = skills[i]
                        val animatedProficiency = proficiency * animationTrigger.value
                        val angle = -PI.toFloat() / 2f + i * (2f * PI.toFloat() / n)
                        val rx = cx + maxRadius * animatedProficiency * cos(angle)
                        val ry = cy + maxRadius * animatedProficiency * sin(angle)
                        if (i == 0) {
                            dataPath.moveTo(rx, ry)
                        } else {
                            dataPath.lineTo(rx, ry)
                        }
                        dataPoints.add(Offset(rx, ry))
                    }
                    dataPath.close()
                    
                    // Fill Recharts area
                    drawPath(
                        path = dataPath,
                        color = accentColor.copy(alpha = 0.22f)
                    )
                    // Outline stroke
                    drawPath(
                        path = dataPath,
                        color = accentColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                    
                    // Draw dot markers at each vertex (same style as Recharts)
                    dataPoints.forEach { pt ->
                        drawCircle(
                            color = accentColor,
                            radius = 5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = surfaceColor,
                            radius = 2.5.dp.toPx(),
                            center = pt
                        )
                    }
                    
                    // Draw responsive non-overlapping labels
                    for (i in 0 until n) {
                        val (skillName, _) = skills[i]
                        val angle = -PI.toFloat() / 2f + i * (2f * PI.toFloat() / n)
                        
                        // Measure text layout safely
                        val textLayoutResult = textMeasurer.measure(
                            text = skillName,
                            style = TextStyle(
                                color = onSurfaceColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                        
                        val textWidth = textLayoutResult.size.width
                        val textHeight = textLayoutResult.size.height
                        
                        val labelRadius = maxRadius + 14.dp.toPx()
                        val lx = cx + labelRadius * cos(angle)
                        val ly = cy + labelRadius * sin(angle)
                        
                        val xOffset = when {
                            cos(angle) > 0.15f -> 0f
                            cos(angle) < -0.15f -> -textWidth.toFloat()
                            else -> -textWidth / 2f
                        }
                        val yOffset = when {
                            sin(angle) > 0.15f -> 0f
                            sin(angle) < -0.15f -> -textHeight.toFloat()
                            else -> -textHeight / 2f
                        }
                        
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(lx + xOffset, ly + yOffset)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedSkillsView() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InteractiveRadarChart()
        
        SkillCategory(
            title = trans("Programming & Languages", "Ohjelmointi & ohjelmointikielet"),
            skills = listOf(
                Pair("Kotlin & Java", 0.95f),
                Pair("TypeScript & JS", 0.85f),
                Pair("Go (Golang)", 0.80f),
                Pair("SQL & HTML/CSS", 0.75f)
            )
        )
        SkillCategory(
            title = trans("Frontend & App Frameworks", "Käyttöliittymät & sovelluskehykset"),
            skills = listOf(
                Pair("Jetpack Compose", 0.98f),
                Pair("React (Next.js)", 0.85f),
                Pair("Coroutines & Flow", 0.92f),
                Pair("Adaptive/Responsive Design", 0.90f)
            )
        )
        SkillCategory(
            title = trans("Tooling, DevOps & Cloud", "Työkalut, DevOps & pilvipalvelut"),
            skills = listOf(
                Pair("Git, GitHub & Actions", 0.90f),
                Pair("Docker & Containerization", 0.80f),
                Pair("AWS Deployments", 0.70f),
                Pair("Firebase Suite", 0.88f)
            )
        )
    }
}

@Composable
fun SkillCategory(title: String, skills: List<Pair<String, Float>>) {
    ScrollIntersectionObserverWrapper {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                skills.forEach { (skill, proficiency) ->
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = skill,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val levelText = when {
                                proficiency >= 0.95f -> trans("Expert", "Asiantuntija")
                                proficiency >= 0.85f -> trans("Advanced", "Edistynyt")
                                proficiency >= 0.75f -> trans("Proficient", "Osaava")
                                else -> trans("Competent", "Pätevä")
                            }
                            Text(
                                text = "$levelText (${(proficiency * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { proficiency },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactSection() {
    val currentLang = LocalLanguage.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    var isSubmitting by remember { mutableStateOf(false) }
    var submitSuccess by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = trans("Contact", "Ota yhteyttä"),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = trans(
                    "Let's co-create something incredible. Reach out for projects, partnerships, or engineering inquiries.",
                    "Luodaan yhdessä jotain merkityksellistä. Ota yhteyttä projekteihin, yhteistyöhön tai muihin kyselyihin liittyen."
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Quick social contact grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f).clickable { /* Action */ }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mail,
                            contentDescription = "Mail Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = trans("Email Me", "Sähköposti"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "jaakko.kkallio@gmail.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f).clickable { /* Action */ }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = "LinkedIn Icon",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "LinkedIn",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = trans("Let's connect", "Verkostoidutaan"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Contact Form Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = trans("Send a Message", "Lähetä viesti"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                AnimatedVisibility(
                    visible = submitSuccess,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Success tick",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = trans("Message Sent Successfully!", "Viesti lähetetty onnistuneesti!"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = trans(
                                "Thank you for reaching out, $name. I've received your request and will follow up shortly.",
                                "Kiitos yhteydenotostasi, $name. Olen vastaanottanut tiedustelusi ja palaan asiaan pian."
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                name = ""
                                email = ""
                                message = ""
                                submitSuccess = false
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(trans("Send Another Message", "Lähetä uusi viesti"))
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !submitSuccess,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Name input
                        OutlinedTextField(
                            value = name,
                            onValueChange = { 
                                name = it
                                errorText = null
                            },
                            label = { Text(trans("Name", "Nimi")) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Name Icon",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Email input
                        OutlinedTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                errorText = null
                            },
                            label = { Text(trans("Email Address", "Sähköpostiosoite")) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Mail,
                                    contentDescription = "Email Icon",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Message input
                        OutlinedTextField(
                            value = message,
                            onValueChange = { 
                                message = it
                                errorText = null
                            },
                            label = { Text(trans("Your Message", "Viestisi")) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = "Chat Message Icon",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            minLines = 4,
                            maxLines = 6,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Error message feedback
                        if (errorText != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Error detail",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = errorText!!,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Submit action button
                        Button(
                            onClick = {
                                when {
                                    name.isBlank() -> {
                                        errorText = transWithLanguage(currentLang, "Please enter your name", "Syötä nimesi")
                                    }
                                    email.isBlank() -> {
                                        errorText = transWithLanguage(currentLang, "Please enter your email", "Syötä sähköpostiosoitteesi")
                                    }
                                    !email.matches(emailRegex) -> {
                                        errorText = transWithLanguage(currentLang, "Please enter a valid email address", "Sähköpostiosoite on virheellinen")
                                    }
                                    message.isBlank() -> {
                                        errorText = transWithLanguage(currentLang, "Please type your message", "Kirjoita viestisi")
                                    }
                                    else -> {
                                        isSubmitting = true
                                        errorText = null
                                        coroutineScope.launch {
                                            delay(1500) // Simulate networking
                                            isSubmitting = false
                                            submitSuccess = true
                                        }
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = trans("Sending...", "Lähetetään..."),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = "Send",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = trans("Send Message", "Lähetä viesti"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkillChip(text: String) {
  Surface(
    shape = RoundedCornerShape(6.dp),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onPrimaryContainer,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      fontWeight = FontWeight.SemiBold
    )
  }
}

@Composable
fun StatsCard(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
  Card(
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
    modifier = modifier.clickable { }
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
      )
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun BottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
  Surface(
    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
    tonalElevation = 0.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .windowInsetsPadding(WindowInsets.navigationBars),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NavBarItem(icon = Icons.Outlined.Home, label = trans("Home", "Koti"), isSelected = selectedTab == "Home", onClick = { onTabSelected("Home") })
      NavBarItem(icon = Icons.Outlined.GridView, label = trans("Projects", "Projektit"), isSelected = selectedTab == "Projects", onClick = { onTabSelected("Projects") })
      NavBarItem(icon = Icons.Outlined.Person, label = trans("About", "Minusta"), isSelected = selectedTab == "About", onClick = { onTabSelected("About") })
      NavBarItem(icon = Icons.Outlined.Mail, label = trans("Contact", "Yhteys"), isSelected = selectedTab == "Contact", onClick = { onTabSelected("Contact") })
    }
  }
}

@Composable
fun NavBarItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(16.dp))
        .background(
          if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
        )
        .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 4.dp),
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
    )
  }
}


